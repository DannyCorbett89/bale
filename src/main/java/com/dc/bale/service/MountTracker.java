package com.dc.bale.service;

import com.dc.bale.component.HttpClient;
import com.dc.bale.database.*;
import com.dc.bale.exception.MountException;
import com.dc.bale.model.AvailableMount;
import com.dc.bale.model.MountRS;
import com.dc.bale.model.PlayerRS;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MountTracker {
    private static final Logger LOG = Logger.getLogger(MountTracker.class.getName());

    private static final String BASE_URL = "https://na.finalfantasyxiv.com";

    @NonNull
    private HttpClient httpClient;
    @NonNull
    private MountRepository mountRepository;
    @NonNull
    private PlayerRepository playerRepository;
    @NonNull
    private MountLinkRepository mountLinkRepository;
    @NonNull
    private TrialRepository trialRepository;
    @NonNull
    private ConfigRepository configRepository;

    private Map<String, List<Mount>> playerMounts = new HashMap<>();
    private String lastUpdated = "Never";

    public String getLastUpdated() {
        return lastUpdated;
    }

    public List<PlayerRS> getMounts() {
        List<PlayerRS> players = playerMounts.entrySet().stream().map(stringListEntry -> PlayerRS.builder()
                .name(stringListEntry.getKey())
                .mounts(stringListEntry.getValue().stream().map(mount -> MountRS.builder()
                        .id(mount.getId())
                        .name(mount.getName())
                        .instance(getInstance(mount))
                        .build()).collect(Collectors.toList()))
                .build()).collect(Collectors.toList());

        for (PlayerRS player : players) {
            for (int i = player.getMounts().size() - 1; i >= 0; i--) {
                MountRS mount = player.getMounts().get(i);
                if (!anyPlayerHasMount(players, mount)) {
                    player.getMounts().remove(i);
                }
            }
        }

        players.sort((o1, o2) -> Long.compare(o2.numMounts(), o1.numMounts()));
        players.forEach(player -> player.getMounts().sort(Comparator.comparingLong(MountRS::getId)));

        return players;
    }

    public void addMount(String name) throws MountException {
        Mount mount = mountRepository.findByName(name);

        if (mount == null) {
            throw new MountException("Unknown mount: " + name);
        } else {
            mount.setTracking(true);
            mountRepository.save(mount);
        }

        loadMounts();
    }

    public void removeMount(long id) throws MountException {
        Mount mount = mountRepository.findOne(id);

        if (mount == null) {
            throw new MountException("Unknown mount: " + id);
        } else {
            mount.setTracking(false);
            mountRepository.save(mount);
        }

        loadMounts();
    }

    @PostConstruct
    @Scheduled(cron = "0 0 * * * *")
    public void loadMounts() {
        LOG.info("Loading Mounts");

        Map<String, Mount> totalMounts = mountRepository.findAllByTracking(true).stream()
                .collect(Collectors.toMap(Mount::getName, mount -> mount));

        // Get the list of all mounts that each player has, either from database or website
        List<Player> players = loadPlayerData(totalMounts);

        // Convert it to a list of mounts that each player does not
        // have from the list of total mounts
        playerMounts.clear();
        playerMounts.putAll(players.stream()
                .collect(Collectors.toMap(Player::getName, player -> getMissingMounts(player, totalMounts))));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        lastUpdated = sdf.format(new Date());
    }

    private List<Player> loadPlayerData(Map<String, Mount> totalMounts) {
        Config freeCompanyUrl = configRepository.findByName("freeCompanyUrl");
        Optional<String> response = httpClient.get(BASE_URL + freeCompanyUrl.getValue());
        if (!response.isPresent()) {
            return Collections.emptyList();
        }
        String content = response.get();

        Map<String, Player> players = playerRepository.findAll().stream()
                .collect(Collectors.toMap(Player::getName, player -> player));
        Map<Player, MountLoader> loaders = new HashMap<>();
        int numPages = getNumPages(content);
        Set<Player> existingPlayers = new HashSet<>();

        for (int x = 1; x <= numPages; x++) {
            // First page is already loaded, don't load it again
            if (x > 1) {
                Optional<String> nextPageResponse = httpClient.get(BASE_URL + freeCompanyUrl.getValue() + "?page=" + x);
                if (!nextPageResponse.isPresent()) {
                    return Collections.emptyList();
                }
                content = nextPageResponse.get();
            }
            Pattern pattern = Pattern.compile("<li class=\"entry\"><a href=\"(.+?)\".+?<p class=\"entry__name\">(.+?)</p>.+?</li>.+?</li>.+?</li>");
            Matcher matcher = pattern.matcher(content);

            // Load all the mounts for each player from the lodestone
            while (matcher.find()) {
                String playerUrl = matcher.group(1);
                String playerName = matcher.group(2).replace("&#39;", "'");
                Player player;

                if (players.containsKey(playerName)) {
                    player = players.get(playerName);
                } else {
                    player = playerRepository.save(Player.builder()
                            .name(playerName)
                            .url(playerUrl)
                            .build());
                }

                if (player.isTracking()) {
                    MountLoader mountLoader = new MountLoader(player, totalMounts);
                    mountLoader.start();
                    loaders.put(player, mountLoader);
                }
                existingPlayers.add(player);
            }
        }

        // Wait for all loaders to finish processing
        loaders.values().forEach(mountLoader -> {
            try {
                mountLoader.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Set<Player> oldPlayers = players.values().stream()
                .filter(player -> !existingPlayers.contains(player))
                .collect(Collectors.toSet());

        playerRepository.delete(oldPlayers);

        return players.values().stream()
                .filter(player -> playerRepository.exists(player.getId()))
                .filter(Player::isTracking)
                .collect(Collectors.toList());
    }

    public List<AvailableMount> getAvailableMounts() {
        return mountRepository.findAllByTracking(false).stream()
                .map(mount -> AvailableMount.builder()
                        .id(mount.getId())
                        .name(mount.getName())
                        .build())
                .sorted(Comparator.comparing(AvailableMount::getName))
                .collect(Collectors.toList());
    }

    private int getNumPages(String content) {
        Pattern pattern = Pattern.compile("<div class=\"parts__total\">([0-9]+) Total</div>");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            int numMembers = Integer.parseInt(matcher.group(1));
            int numPages = numMembers / 50;

            if (numMembers % 50 != 0) {
                numPages++;
            }

            return numPages;
        }

        return 1;
    }

    private String getInstance(Mount mount) {
        if (mount.getName() == null) {
            return null;
        }

        List<String> trialBossNames = mountLinkRepository.findAllByMountIdAndTrialIdGreaterThan(mount.getId(), 0).stream()
                .map(mountLink -> trialRepository.findOne(mountLink.getTrialId()).getBoss())
                .collect(Collectors.toList());

        if (trialBossNames.size() == 0 || trialBossNames.size() > 1) {
            return mount.getName();
        } else {
            return StringUtils.join(trialBossNames, "/");
        }
    }

    private boolean anyPlayerHasMount(List<PlayerRS> players, MountRS mount) {
        for (PlayerRS player : players) {
            for (MountRS mountRS : player.getMounts()) {
                if (mountRS.getId() == mount.getId() && mountRS.getName() != null) {
                    return true;
                }
            }
        }

        return false;
    }

    private List<Mount> getMissingMounts(Player player, Map<String, Mount> totalMounts) {
        return totalMounts.values().stream()
                .map(mount -> !player.getMounts().contains(mount) ? mount : Mount.builder().id(mount.getId()).build())
                .collect(Collectors.toList());
    }

    void trackPlayer(Player player) {
        Map<String, Mount> totalMounts = mountRepository.findAllByTracking(true).stream()
                .collect(Collectors.toMap(Mount::getName, mount -> mount));

        loadMounts(player, totalMounts);

        playerMounts.put(player.getName(), getMissingMounts(player, totalMounts));
    }

    void untrackPlayer(Player player) {
        playerMounts.remove(player.getName());
    }

    private void loadMounts(Player player, Map<String, Mount> totalMounts) {
        new MountLoader(player, totalMounts).run();
    }

    public class MountLoader extends Thread {
        private Player player;
        private Map<String, Mount> totalMounts;

        MountLoader(Player player, Map<String, Mount> totalMounts) {
            this.player = player;
            this.totalMounts = totalMounts;
        }

        @Override
        public void run() {
            // If all the mounts that we are checking for in totalMounts exist in the player's
            // list of mounts, there will be no point in loading the URL
            if (totalMounts.values().stream().allMatch(mount -> player.hasMount(mount.getName()))) {
                return;
            }

            String url = BASE_URL + player.getUrl();
            Optional<String> response = httpClient.get(url);
            if (!response.isPresent()) {
                return;
            }
            String content = response.get();
            Pattern pattern = Pattern.compile("<li><div class=\"character__item_icon.+?data-tooltip=\"(.+?)\".+?</li>");
            Matcher matcher = pattern.matcher(content);
            boolean modified = false;

            while (matcher.find()) {
                String mount = matcher.group(1);

                if (!player.hasMount(mount) && player.addMount(totalMounts.get(mount))) {
                    modified = true;
                }
            }

            if (modified) {
                playerRepository.save(player);
            }
        }
    }
}
