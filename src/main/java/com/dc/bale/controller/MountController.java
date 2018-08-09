package com.dc.bale.controller;

import com.dc.bale.component.HttpClient;
import com.dc.bale.component.JsonConverter;
import com.dc.bale.database.*;
import com.dc.bale.model.MountRS;
import com.dc.bale.model.PlayerRS;
import com.dc.bale.model.Response;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequestMapping("/")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MountController {
    private static final String BASE_URL = "https://na.finalfantasyxiv.com";
    private static final Map<String, List<Mount>> PLAYER_MOUNTS = new HashMap<>();

    @NonNull
    private HttpClient httpClient;
    @NonNull
    private JsonConverter jsonConverter;
    @NonNull
    private MountRepository mountRepository;
    @NonNull
    private PlayerRepository playerRepository;
    @NonNull
    private ConfigRepository configRepository;

    private String lastUpdated = "Never";

    @RequestMapping(value = "/players", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> listPlayers() throws IOException {
        List<Player> players = playerRepository.findByTrackingFalse();

        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .body(jsonConverter.toString(players));
    }

    @RequestMapping(value = "/json", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> listMountsJSON(@RequestParam(value = "refresh", required = false) String refresh) throws IOException {
        if (refresh != null && refresh.equals("true")) {
            loadMounts();
        }

        Response response = Response.builder()
                .lastUpdated(lastUpdated)
                .players(PLAYER_MOUNTS.entrySet().stream().map(stringListEntry -> PlayerRS.builder()
                        .name(stringListEntry.getKey())
                        .mounts(stringListEntry.getValue().stream().map(mount -> MountRS.builder()
                                .id(mount.getId())
                                .name(mount.getName())
                                .instance(mount.getInstance())
                                .build()).collect(Collectors.toList()))
                        .build()).collect(Collectors.toList()))
                .build();

        List<PlayerRS> players = response.getPlayers();

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

        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .body(jsonConverter.toString(response));
    }

    private boolean anyPlayerHasMount(List<PlayerRS> players, MountRS mount) {
        for (PlayerRS player : players) {
            for (MountRS mountRS : player.getMounts()) {
                if (mountRS.getId() == mount.getId() && mountRS.getName() != null && mountRS.getInstance() != null) {
                    return true;
                }
            }
        }

        return false;
    }

    @RequestMapping(value = "/addPlayer", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> addPlayer(@RequestParam("playerId") long playerId) throws IOException {
        String response;
        try {
            Map<String, Mount> totalMounts = mountRepository.findAll().stream()
                    .collect(Collectors.toMap(Mount::getName, mount -> mount));

            Player player = playerRepository.findOne(playerId);
            player.setTracking(true);
            player = playerRepository.save(player);
            loadMounts(player, totalMounts);

            PLAYER_MOUNTS.put(player.getName(), getMissingMounts(player, totalMounts));

            response = jsonConverter.toString(StatusResponse.success());
        } catch (Exception e) {
            response = jsonConverter.toString(StatusResponse.error(e.getLocalizedMessage()));
        }
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .body(jsonConverter.toString(response));
    }

    @RequestMapping(value = "/removePlayer", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> removePlayer(@RequestParam(value = "playerName") String playerName) throws IOException {
        String response;
        try {
            Player player = playerRepository.findByName(playerName);

            if (player != null) {
                player.setTracking(false);
                playerRepository.save(player);
                PLAYER_MOUNTS.remove(player.getName());
                response = jsonConverter.toString(StatusResponse.success());
            } else {
                response = jsonConverter.toString(StatusResponse.error("Player not found: " + playerName));
            }
        } catch (Exception e) {
            response = jsonConverter.toString(StatusResponse.error(e.getLocalizedMessage()));
        }
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .body(jsonConverter.toString(response));
    }

    @Scheduled(fixedRate = 3600000)
    public void loadMounts() throws IOException {
        Map<String, Mount> totalMounts = mountRepository.findAll().stream()
                .collect(Collectors.toMap(Mount::getName, mount -> mount));

        // Get the list of all mounts that each player has, either from database or website
        List<Player> players = loadPlayerData(totalMounts);

        // Convert it to a list of mounts that each player does not
        // have from the list of total mounts
        PLAYER_MOUNTS.clear();
        PLAYER_MOUNTS.putAll(players.stream()
                .collect(Collectors.toMap(Player::getName, player -> getMissingMounts(player, totalMounts))));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        lastUpdated = sdf.format(new Date());
    }

    private void loadMounts(Player player, Map<String, Mount> totalMounts) {
        new MountLoader(player, totalMounts).run();
    }

    private List<Mount> getMissingMounts(Player player, Map<String, Mount> totalMounts) {
        return totalMounts.values().stream()
                .map(mount -> !player.getMounts().contains(mount) ? mount : Mount.builder().id(mount.getId()).build())
                .collect(Collectors.toList());
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
            try {
                String content = httpClient.get(url);
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    private List<Player> loadPlayerData(Map<String, Mount> totalMounts) throws IOException {
        Config freeCompanyUrl = configRepository.findByName("freeCompanyUrl");
        String content = httpClient.get(BASE_URL + freeCompanyUrl.getValue());

        Map<String, Player> players = playerRepository.findAll().stream()
                .collect(Collectors.toMap(Player::getName, player -> player));
        Map<Player, MountLoader> loaders = new HashMap<>();
        int numPages = getNumPages(content);

        for (int x = 1; x <= numPages; x++) {
            // First page is already loaded, don't load it again
            if (x > 1) {
                content = httpClient.get(BASE_URL + freeCompanyUrl.getValue() + "?page=" + x);
            }
            Pattern pattern = Pattern.compile("<li class=\"entry\"><a href=\"(.+?)\".+?<p class=\"entry__name\">(.+?)<\\/p>.+?<\\/li>.+?<\\/li>.+?<\\/li>");
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

        return players.values().stream()
                .filter(Player::isTracking)
                .collect(Collectors.toList());
    }
}
