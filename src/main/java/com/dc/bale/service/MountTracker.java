package com.dc.bale.service;

import com.dc.bale.component.HttpClient;
import com.dc.bale.database.*;
import com.dc.bale.exception.MountException;
import com.dc.bale.model.AvailableMount;
import com.dc.bale.model.MinionRS;
import com.dc.bale.model.MountRS;
import com.dc.bale.model.PlayerRS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MountTracker {
    private static final String BASE_URL = "https://na.finalfantasyxiv.com";

    private final HttpClient httpClient;
    private final MountRepository mountRepository;
    private final PlayerRepository playerRepository;
    private final FcRankRepository rankRepository;
    private final MountLinkRepository mountLinkRepository;
    private final TrialRepository trialRepository;
    private final MinionRepository minionRepository;
    private final ConfigRepository configRepository;

    private Map<String, List<Mount>> playerMounts = new HashMap<>();
    private Map<String, List<Minion>> playerMinions = new HashMap<>();
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

        Map<Long, Long> ilevels = mountLinkRepository.findAll().stream()
                .filter(mountLink -> mountLink.getTrialId() > 0)
                .collect(Collectors.toMap(
                        MountLink::getMountId,
                        mountLink -> {
                            Trial trial = trialRepository.findOne(mountLink.getTrialId());
                            if (trial != null) {
                                return trial.getItemLevel();
                            } else {
                                return 0L;
                            }
                        },
                        (first, second) -> first)
                );

        players.sort((o1, o2) -> Long.compare(o2.numMounts(), o1.numMounts()));
        players.forEach(player -> player.getMounts().sort(Comparator.comparingLong(mount -> ilevels.get(mount.getId()))));

        return players;
    }

    public List<PlayerRS> getMinions() {
        // TODO: minion 205 is coming back as null
        Map<String, Player> playersDB = playerRepository.findByNameIn(playerMinions.keySet()).stream().collect(Collectors.toMap(Player::getName, player -> player));

        return playerMinions.entrySet().stream().map(stringListEntry -> PlayerRS.builder()
                .id(playersDB.get(stringListEntry.getKey()).getId())
                .name(stringListEntry.getKey())
                .minions(stringListEntry.getValue().stream()
                        .filter(minion -> minion.getName() != null && minion.getLodestoneId() != null)
                        .map(minion -> MinionRS.builder()
                                .id(minion.getId())
                                .name(minion.getDisplayName())
                                .url(minion.getUrl())
                                .build())
                        .collect(Collectors.toMap(minion -> "minion-" + minion.getId(), MinionRS::getName)))
                .build()).sorted((o1, o2) -> Long.compare(o2.numMinions(), o1.numMinions())).collect(Collectors.toList());
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

    public Mount removeMount(long id) throws MountException {
        Mount mount = mountRepository.findOne(id);

        if (mount == null) {
            throw new MountException("Unknown mount: " + id);
        } else {
            mount.setTracking(false);
            mountRepository.save(mount);
        }

        loadMounts();

        return mount;
    }

    @PostConstruct
    @Scheduled(cron = "0 0 * * * *")
    public void loadMounts() {
        Map<String, Mount> totalMounts = mountRepository.findAllByTracking(true).stream()
                .collect(Collectors.toMap(Mount::getName, mount -> mount));
        Map<String, Minion> totalMinions = minionRepository.findAll().stream()
                .collect(Collectors.toMap(Minion::getName, minion -> minion));

        // Get the list of all mounts that each player has, either from database or website
        List<Player> players = loadPlayerData(totalMounts, totalMinions);

        // TODO: Store in transient field on player object?
        // Convert it to a list of mounts that each player does not
        // have from the list of total mounts
        playerMounts.clear();
        playerMounts.putAll(players.stream()
                .collect(Collectors.toMap(Player::getName, player -> getMissingMounts(player, totalMounts))));


        playerMinions.clear();
        playerMinions.putAll(players.stream()
                .collect(Collectors.toMap(Player::getName, player -> getMissingMinions(player, totalMinions))));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        lastUpdated = sdf.format(new Date());
    }

    private List<Player> loadPlayerData(Map<String, Mount> totalMounts, Map<String, Minion> totalMinions) {
        Config freeCompanyUrl = configRepository.findByName("freeCompanyUrl");
        String content = httpClient.get(BASE_URL + freeCompanyUrl.getValue());

        Map<String, Player> players = playerRepository.findAll().stream()
                .collect(Collectors.toMap(Player::getName, player -> player));
        Map<Player, Loader> loaders = new HashMap<>();
        int numPages = getNumPages(content);
        Set<Player> existingPlayers = new HashSet<>();

        for (int x = 1; x <= numPages; x++) {
            // First page is already loaded, don't load it again
            if (x > 1) {
                content = httpClient.get(BASE_URL + freeCompanyUrl.getValue() + "?page=" + x);
            }
            Pattern pattern = Pattern.compile("<li class=\"entry\"><a href=\"(.+?)\".+?<p class=\"entry__name\">(.+?)</p>.+?<ul class=\"entry__freecompany__info\"><li><img src=\"(.+?)\".+?<span>(.+?)</span></li>.+?</li>.+?</li>");
            Matcher matcher = pattern.matcher(content);

            // Load all the mounts for each player from the lodestone
            while (matcher.find()) {
                String playerUrl = matcher.group(1);
                String playerName = matcher.group(2).replace("&#39;", "'");
                Player player;
                String rankIcon = matcher.group(3);
                String rankName = matcher.group(4);
                FcRank rank = rankRepository.findByIcon(rankIcon);

                if (rank == null) {
                    rank = rankRepository.save(FcRank.builder()
                            .name(rankName)
                            .icon(rankIcon)
                            .build());
                } else if (rank.getName() != null && !rank.getName().equals(rankName)) {
                    rank.setName(rankName);
                    rank = rankRepository.save(rank);
                }

                if (players.containsKey(playerName)) {
                    player = players.get(playerName);

                    if (player.getRank() == null || player.getRank().getId() != rank.getId()) {
                        player.setRank(rank);
                        player = playerRepository.save(player);
                    }
                } else {
                    player = playerRepository.save(Player.builder()
                            .name(playerName)
                            .url(playerUrl)
                            .rank(rank)
                            .build());
                }

                if (player.isTracking()) {
                    MountLoader mountLoader = new MountLoader(player, totalMounts);
                    mountLoader.start();
                    loaders.put(player, mountLoader);


                    MinionLoader minionLoader = new MinionLoader(player, totalMinions);
                    minionLoader.start();
                    loaders.put(player, minionLoader);
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


        // TODO: Potential bug here. Every now and then, players are unintentionally untracked
        log.debug("Deleting players: " + oldPlayers.toString());
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

        if (trialBossNames.size() != 1) {
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

    private boolean anyPlayerHasMinion(List<PlayerRS> players, MinionRS minion) {
        for (PlayerRS player : players) {
//            for (MinionRS minionRS : player.getMinions().values()) {
//                if (minionRS.getId() == minion.getId() && minionRS.getName() != null) {
//                    return true;
//                }
//            }
        }

        return false;
    }

    private List<Mount> getMissingMounts(Player player, Map<String, Mount> totalMounts) {
        return totalMounts.values().stream()
                .map(mount -> !player.getMounts().contains(mount) ? mount : Mount.builder().id(mount.getId()).build())
                .collect(Collectors.toList());
    }

    private List<Minion> getMissingMinions(Player player, Map<String, Minion> totalMinions) {
        return totalMinions.values().stream()
                .map(minion -> !player.getMinions().contains(minion) ? minion : Minion.builder().id(minion.getId()).build())
                .collect(Collectors.toList());
    }

    void trackPlayer(Player player) {
        Map<String, Mount> totalMounts = mountRepository.findAllByTracking(true).stream()
                .collect(Collectors.toMap(Mount::getName, mount -> mount));

        loadMounts(player, totalMounts);

        playerMounts.put(player.getName(), getMissingMounts(player, totalMounts));


        Map<String, Minion> totalMinions = minionRepository.findAll().stream()
                .collect(Collectors.toMap(Minion::getName, minion -> minion));

        loadMinions(player, totalMinions);

        playerMinions.put(player.getName(), getMissingMinions(player, totalMinions));
    }

    void untrackPlayer(Player player) {
        playerMounts.remove(player.getName());
    }

    private void loadMounts(Player player, Map<String, Mount> totalMounts) {
        new MountLoader(player, totalMounts).run();
    }

    private void loadMinions(Player player, Map<String, Minion> totalMinions) {
        new MinionLoader(player, totalMinions).run();
    }

    // TODO: Loaders package
    public abstract class Loader extends Thread {
        Player player;

        Loader(Player player) {
            this.player = player;
        }

        protected abstract boolean alreadyOwnsAll();

        protected abstract String getContentSection(String content);

        protected abstract boolean handleMatch(String name);

        @Override
        public void run() {
            if (alreadyOwnsAll()) {
                return;
            }

            String url = BASE_URL + player.getUrl();
            String content = httpClient.get(url);
            String contentSection = getContentSection(content);
            Pattern pattern = Pattern.compile("<li><div class=\"character__item_icon.+?data-tooltip=\"(.+?)\".+?</li>");
            Matcher matcher = pattern.matcher(contentSection);
            boolean modified = false;

            while (matcher.find()) {
                modified = handleMatch(matcher.group(1));
            }

            if (modified) {
                playerRepository.save(player);
            }
        }
    }

    public class MountLoader extends Loader {
        private Map<String, Mount> totalMounts;

        MountLoader(Player player, Map<String, Mount> totalMounts) {
            super(player);
            this.totalMounts = totalMounts;
        }

        @Override
        protected boolean alreadyOwnsAll() {
            return totalMounts.values().stream().allMatch(mount -> player.hasMount(mount.getName()));
        }

        @Override
        protected String getContentSection(String content) {
            return content.substring(0, content.indexOf("Minions</h3>"));
        }

        @Override
        protected boolean handleMatch(String name) {
            return !player.hasMount(name) && player.addMount(totalMounts.get(name));
        }
    }

    public class MinionLoader extends Loader {
        private Map<String, Minion> totalMinions;

        MinionLoader(Player player, Map<String, Minion> totalMinions) {
            super(player);
            this.totalMinions = totalMinions;
        }

        @Override
        protected boolean alreadyOwnsAll() {
            return totalMinions.values().stream().allMatch(minion -> player.hasMinion(minion.getName()));
        }

        @Override
        protected String getContentSection(String content) {
            return content.substring(content.indexOf("Minions</h3>"));
        }

        @Override
        protected boolean handleMatch(String name) {
            return !player.hasMinion(name) && player.addMinion(totalMinions.get(name));
        }
    }
}
