package com.dc.bale.service;

import com.dc.bale.component.HttpClient;
import com.dc.bale.database.dao.FcRankRepository;
import com.dc.bale.database.dao.PlayerRepository;
import com.dc.bale.database.entity.FcRank;
import com.dc.bale.database.entity.Minion;
import com.dc.bale.database.entity.Mount;
import com.dc.bale.database.entity.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dc.bale.Constants.BASE_URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcLoader {
    private final HttpClient httpClient;
    private final MountService mountService;
    private final PlayerRepository playerRepository;
    private final FcRankRepository rankRepository;
    private final MinionService minionService;
    private final ConfigService configService;

    void loadPlayerMounts() {
        loadPlayer("mounts", MountLoader::new);
    }

    void loadPlayerMinions() {
        loadPlayer("minions", MinionLoader::new);
    }

    void loadPlayer(String type, Function<Player, PlayerLoader> getLoader) {
        log.info("Loading player {}...", type);
        String freeCompanyUrl = configService.getConfig("freeCompanyUrl");
        String content = httpClient.get(BASE_URL + freeCompanyUrl);

        Map<String, Player> players = playerRepository.findAll().stream()
                .collect(Collectors.toMap(Player::getName, player -> player));
        int numPages = getNumPages(content);
        List<PlayerLoader> playerLoaders = new ArrayList<>();

        for (int x = 1; x <= numPages; x++) {
            // First page is already loaded, don't load it again
            if (x > 1) {
                content = httpClient.get(BASE_URL + freeCompanyUrl + "?page=" + x);
            }
            playerLoaders.addAll(loadFromPlayerPage(content, players, getLoader));
        }

        playerLoaders.forEach(playerLoader -> {
            try {
                playerLoader.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        log.info("Finished loading player {}", type);
    }

    private List<PlayerLoader> loadFromPlayerPage(String content, Map<String, Player> players, Function<Player, PlayerLoader> getLoader) {
        List<PlayerLoader> playerLoaders = new ArrayList<>();
        Pattern pattern = Pattern.compile("<li class=\"entry\"><a href=\"(.+?)\".+?<div class=\"entry__chara__face\"><img src=\"(.+?)\".+?<p class=\"entry__name\">(.+?)</p>.+?<ul class=\"entry__freecompany__info\"><li><img src=\"(.+?)\".+?<span>(.+?)</span></li>.+?</li>.+?</li>");
        Matcher matcher = pattern.matcher(content);

        // Load all the mounts for each player from the lodestone
        while (matcher.find()) {
            String playerUrl = matcher.group(1);
            String playerIcon = matcher.group(2);
            String playerName = matcher.group(3).replace("&#39;", "'");
            String rankIcon = matcher.group(4);
            String rankName = matcher.group(5);
            FcRank rank = loadRank(rankIcon, rankName);
            Player player = loadPlayer(players, playerName, playerUrl, rank, playerIcon);

            PlayerLoader playerLoader = getLoader.apply(player);
            playerLoader.start();
            playerLoaders.add(playerLoader);
        }

        return playerLoaders;
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

    private Player loadPlayer(Map<String, Player> players, String playerName, String playerUrl, FcRank rank, String playerIcon) {
        Player player;
        if (players.containsKey(playerName)) {
            player = players.get(playerName);
            player.setIcon(playerIcon);

            if (player.getRank() == null || player.getRank().getId() != rank.getId()) {
                player.setRank(rank);
                player = playerRepository.save(player);
            }
        } else {
            player = playerRepository.save(Player.builder()
                    .name(playerName)
                    .url(playerUrl)
                    .rank(rank)
                    .icon(playerIcon)
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

    String getFCPageContent() {
        return getFCPageContent(0);
    }

    String getFCPageContent(int page) {
        String freeCompanyUrl = configService.getConfig("freeCompanyUrl");
        String url = BASE_URL + freeCompanyUrl;

        if (page >= 1) {
            url += "?page=" + page;
        }

        return httpClient.get(url);
    }

    public abstract class PlayerLoader extends Thread {
        private Player player;

        PlayerLoader(Player player) {
            this.player = player;
        }

        protected abstract void load(Player player);

        @Override
        public void run() {
            load(player);

            playerRepository.save(player);
        }
    }

    public class MountLoader extends PlayerLoader {
        MountLoader(Player player) {
            super(player);
        }

        @Override
        protected void load(Player player) {
            String content = httpClient.get(BASE_URL + player.getUrl() + "/mount");
            Pattern hashPattern = Pattern.compile("<li class=\"mount__list_icon.+?data-tooltip_href=\"/lodestone/character/.+?/mount/tooltip/(.+?)\".+?</li>");
            Matcher hashMatcher = hashPattern.matcher(content);

            while (hashMatcher.find()) {
                String hash = hashMatcher.group(1);
                Supplier<String> lookupMountNameFromHash = () -> {
                    String tooltipContent = httpClient.get(BASE_URL + player.getUrl() + "/mount/tooltip/" + hash);
                    Pattern namePattern = Pattern.compile("<h4 class=\"mount__header__label\">(.+?)</h4>");
                    Matcher nameMatcher = namePattern.matcher(tooltipContent);
                    if (nameMatcher.find()) {
                        return nameMatcher.group(1);
                    } else {
                        return null;
                    }
                };
                Optional<Mount> mount = mountService.getAndUpdateMountForHash(player, hash, lookupMountNameFromHash);
                mount.ifPresent(player::addMount);
            }
        }
    }

    public class MinionLoader extends PlayerLoader {
        MinionLoader(Player player) {
            super(player);
        }

        @Override
        protected void load(Player player) {
            String content = httpClient.get(BASE_URL + player.getUrl() + "/minion");
            Pattern hashPattern = Pattern.compile("<li class=\"minion__list_icon.+?data-tooltip_href=\"/lodestone/character/.+?/minion/tooltip/(.+?)\".+?</li>");
            Matcher hashMatcher = hashPattern.matcher(content);

            while (hashMatcher.find()) {
                String hash = hashMatcher.group(1);
                Supplier<String> lookupMinionNameFromHash = () -> {
                    String tooltipContent = httpClient.get(BASE_URL + player.getUrl() + "/minion/tooltip/" + hash);
                    Pattern namePattern = Pattern.compile("<h4 class=\"minion__header__label\">(.+?)</h4>");
                    Matcher nameMatcher = namePattern.matcher(tooltipContent);
                    if (nameMatcher.find()) {
                        String name = nameMatcher.group(1);
                        return HtmlUtils.htmlUnescape(name);
                    } else {
                        return null;
                    }
                };
                Optional<Minion> minion = minionService.getAndUpdateMinionForHash(player, hash, lookupMinionNameFromHash);
                minion.ifPresent(player::addMinion);
            }
        }
    }
}