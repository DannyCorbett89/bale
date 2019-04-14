package com.dc.bale.service;

import com.dc.bale.component.HttpClient;
import com.dc.bale.database.dao.*;
import com.dc.bale.database.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dc.bale.Constants.BASE_URL;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FcLoader {
    private final HttpClient httpClient;
    private final MountRepository mountRepository;
    private final PlayerRepository playerRepository;
    private final FcRankRepository rankRepository;
    private final MinionRepository minionRepository;
    private final ConfigRepository configRepository;

    private Map<String, Mount> totalMounts;
    private Map<String, Minion> totalMinions;

    void loadPlayerData() {
        loadTotals();
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
            loadMountsAndMinionsForPage(content, players);
        }
    }

    private void loadTotals() {
        totalMounts = mountRepository.findAll().stream()
                .collect(Collectors.toMap(mount -> mount.getName().toLowerCase(), mount -> mount));
        totalMinions = minionRepository.findAll().stream()
                .collect(Collectors.toMap(minion -> minion.getName().toLowerCase(), minion -> minion));
    }

    private void loadMountsAndMinionsForPage(String content, Map<String, Player> players) {
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

            new PlayerLoader(player, totalMounts, totalMinions).start();
        }
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

    int getNumPages(String content) {
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

    public class PlayerLoader extends Thread {
        private Player player;
        private Map<String, Mount> totalMounts;
        private Map<String, Minion> totalMinions;

        PlayerLoader(Player player, Map<String, Mount> totalMounts, Map<String, Minion> totalMinions) {
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
            loadData(contentSections[0], name -> player.addMount(totalMounts.get(name.toLowerCase())));
            loadData(contentSections[1], name -> player.addMinion(totalMinions.get(name.toLowerCase())));

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