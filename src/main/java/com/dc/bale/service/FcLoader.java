package com.dc.bale.service;

import com.dc.bale.component.HttpClient;
import com.dc.bale.database.dao.ConfigRepository;
import com.dc.bale.database.dao.FcRankRepository;
import com.dc.bale.database.dao.PlayerRepository;
import com.dc.bale.database.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dc.bale.Constants.BASE_URL;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FcLoader {
    private final HttpClient httpClient;
    private final MountService mountService;
    private final PlayerRepository playerRepository;
    private final FcRankRepository rankRepository;
    private final MinionService minionService;
    private final ConfigService configService;

    void loadPlayerData() {
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
            playerLoaders.addAll(loadMountsAndMinionsForPage(content, players));
        }

        playerLoaders.forEach(playerLoader -> {
            try {
                playerLoader.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        log.info("Finished loading player data");
    }

    private List<PlayerLoader> loadMountsAndMinionsForPage(String content, Map<String, Player> players) {
        List<PlayerLoader> playerLoaders = new ArrayList<>();
        String playersRegex = configService.getConfig("regex_players");
        Pattern pattern = Pattern.compile(playersRegex);
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

            PlayerLoader playerLoader = new PlayerLoader(player);
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
        String numFcPagesRegex = configService.getConfig("regex_num_fc_pages");
        Pattern pattern = Pattern.compile(numFcPagesRegex);
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

    public class PlayerLoader extends Thread {
        private Player player;

        PlayerLoader(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            player.clear();
            loadMounts(player);
            loadMinions(player);

            playerRepository.save(player);

            log.info("Loaded data for {}", player.getName());
        }

        private void loadMounts(Player player) {
            String content = httpClient.get(BASE_URL + player.getUrl() + "/mount");
            String mountHashRegex = configService.getConfig("regex_mount_hash");
            Pattern hashPattern = Pattern.compile(mountHashRegex);
            Matcher hashMatcher = hashPattern.matcher(content);

            while (hashMatcher.find()) {
                String hash = hashMatcher.group(1);
                Supplier<String> lookupMountNameFromHash = () -> {
                    String tooltipContent = httpClient.get(BASE_URL + player.getUrl() + "/mount/tooltip/" + hash);
                    String mountNameRegex = configService.getConfig("regex_mount_name");
                    Pattern namePattern = Pattern.compile(mountNameRegex);
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

        private void loadMinions(Player player) {
            String content = httpClient.get(BASE_URL + player.getUrl() + "/minion");
            String minionHashRegex = configService.getConfig("regex_minion_hash");
            Pattern hashPattern = Pattern.compile(minionHashRegex);
            Matcher hashMatcher = hashPattern.matcher(content);

            while (hashMatcher.find()) {
                String hash = hashMatcher.group(1);
                Supplier<String> lookupMinionNameFromHash = () -> {
                    String tooltipContent = httpClient.get(BASE_URL + player.getUrl() + "/minion/tooltip/" + hash);
                    String minionNameRegex = configService.getConfig("regex_minion_name");
                    Pattern namePattern = Pattern.compile(minionNameRegex);
                    Matcher nameMatcher = namePattern.matcher(tooltipContent);
                    if (nameMatcher.find()) {
                        return nameMatcher.group(1);
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