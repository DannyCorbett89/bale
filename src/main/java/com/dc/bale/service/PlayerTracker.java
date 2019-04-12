package com.dc.bale.service;

import com.dc.bale.component.HttpClient;
import com.dc.bale.database.dao.*;
import com.dc.bale.database.entity.*;
import com.dc.bale.exception.MountException;
import com.dc.bale.model.AvailableMount;
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
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlayerTracker {
    private static final String BASE_URL = "https://na.finalfantasyxiv.com";

    private final HttpClient httpClient;
    private final MountRepository mountRepository;
    private final PlayerRepository playerRepository;
    private final FcRankRepository rankRepository;
    private final MountLinkRepository mountLinkRepository;
    private final TrialRepository trialRepository;
    private final MinionRepository minionRepository;
    private final ConfigRepository configRepository;

    private String lastUpdated = "Never";

    public String getLastUpdated() {
        return lastUpdated;
    }

    public List<PlayerRS> getMounts() {
        List<Player> visiblePlayers = playerRepository.findByVisibleTrue();
        List<Mount> totalMounts = mountRepository.findAll();

        List<PlayerRS> players = visiblePlayers.stream().map(player -> PlayerRS.builder()
                .name(player.getName())
                .mounts(player.getMissingMounts(totalMounts).stream().map(mount -> MountRS.builder()
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

    public void addMount(String name) throws MountException {
        Mount mount = mountRepository.findByName(name);

        if (mount == null) {
            throw new MountException("Unknown mount: " + name);
        } else {
            mount.setVisible(true);
            mountRepository.save(mount);
        }
    }

    public Mount removeMount(long id) throws MountException {
        Mount mount = mountRepository.findOne(id);

        if (mount == null) {
            throw new MountException("Unknown mount: " + id);
        } else {
            mount.setVisible(false);
            mountRepository.save(mount);
        }

        return mount;
    }

    @PostConstruct
    @Scheduled(cron = "0 0 * * * *")
    public void loadMounts() {
        loadPlayerData();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        lastUpdated = sdf.format(new Date());
    }

    private void loadPlayerData() {
        Map<String, Mount> totalMounts = mountRepository.findAll().stream()
                .collect(Collectors.toMap(Mount::getName, mount -> mount));
        Map<String, Minion> totalMinions = minionRepository.findAll().stream()
                .collect(Collectors.toMap(Minion::getName, minion -> minion));
        Config freeCompanyUrl = configRepository.findByName("freeCompanyUrl");
        String content = httpClient.get(BASE_URL + freeCompanyUrl.getValue());

        Map<String, Player> players = playerRepository.findAll().stream()
                .collect(Collectors.toMap(Player::getName, player -> player));
        int numPages = getNumPages(content);

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
                String rankIcon = matcher.group(3);
                String rankName = matcher.group(4);
                FcRank rank = loadRank(rankIcon, rankName);
                Player player = loadPlayer(players, playerName, playerUrl, rank);

                new Loader(player, totalMounts, totalMinions).start();
            }
        }
    }

    @PostConstruct
    @Scheduled(cron = "0 30 * * * *")
    public void cleanOldPlayers() {
        // TODO: Test by manually adding made up player to DB
        // TODO: Refactor, extract FC Page loader to method

        Map<String, Player> dbPlayers = playerRepository.findAll().stream()
                .collect(Collectors.toMap(Player::getName, player -> player));
        Set<String> fcPlayers = new HashSet<>();

        Config freeCompanyUrl = configRepository.findByName("freeCompanyUrl");
        String content = httpClient.get(BASE_URL + freeCompanyUrl.getValue());

        int numPages = getNumPages(content);

        for (int x = 1; x <= numPages; x++) {
            // First page is already loaded, don't load it again
            if (x > 1) {
                content = httpClient.get(BASE_URL + freeCompanyUrl.getValue() + "?page=" + x);
            }
            Pattern pattern = Pattern.compile("<li class=\"entry\"><a href=\"(.+?)\".+?<p class=\"entry__name\">(.+?)</p>.+?<ul class=\"entry__freecompany__info\"><li><img src=\"(.+?)\".+?<span>(.+?)</span></li>.+?</li>.+?</li>");
            Matcher matcher = pattern.matcher(content);

            // Load all the mounts for each player from the lodestone
            while (matcher.find()) {
                String playerName = matcher.group(2).replace("&#39;", "'");
                fcPlayers.add(playerName);
            }
        }
        Set<Player> oldPlayers = dbPlayers.values().stream()
                .filter(player -> !fcPlayers.contains(player.getName()))
                .collect(Collectors.toSet());

        // TODO: Potential bug here. Every now and then, players are removed from the DB, then re-added as visible=0
        log.info("Deleting players: " + oldPlayers.toString());
        playerRepository.delete(oldPlayers);
    }

    private FcRank loadRank(String rankIcon, String rankName) {
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

        return rank;
    }

    private Player loadPlayer(Map<String, Player> players, String playerName, String playerUrl, FcRank rank) {
        Player player;
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
                    .mounts(new HashSet<>())
                    .minions(new HashSet<>())
                    .build());
        }

        return player;
    }

    public List<AvailableMount> getAvailableMounts() {
        return mountRepository.findAllByVisible(false).stream()
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

    public class Loader extends Thread {
        private Player player;
        private Map<String, Mount> totalMounts;
        private Map<String, Minion> totalMinions;

        Loader(Player player, Map<String, Mount> totalMounts, Map<String, Minion> totalMinions) {
            this.player = player;
            this.totalMounts = totalMounts;
            this.totalMinions = totalMinions;
        }

        @Override
        public void run() {
            String url = BASE_URL + player.getUrl();
            String content = httpClient.get(url);

            if (!playerHasData(content)) {
                return;
            }

            player.clear();
            String[] contentSections = content.split("Minions</h3>");
            loadData(contentSections[0], name -> player.addMount(totalMounts.get(name)));
            loadData(contentSections[1], name -> player.addMinion(totalMinions.get(name)));

            playerRepository.save(player);

            log.info("Loaded data for {}", player.getName());
        }

        private boolean playerHasData(String content) {
            return !content.contains("<span class=\"disable\">Mounts/Minions</span>");
        }

        private void loadData(String content, Consumer<String> action) {
            Pattern pattern = Pattern.compile("<li><div class=\"character__item_icon.+?data-tooltip=\"(.+?)\".+?</li>");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                String name = matcher.group(1);
                action.accept(name);
            }
        }
    }
}
