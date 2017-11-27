package com.dc.bale.controller;

import com.dc.bale.component.HttpClient;
import com.dc.bale.database.Mount;
import com.dc.bale.database.MountRepository;
import com.dc.bale.database.Player;
import com.dc.bale.database.PlayerRepository;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// TODO: Switch back to /mounts when there is some content for RootController
@RequestMapping("/")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MountController {
    private static final String BASE_URL = "https://na.finalfantasyxiv.com";
    private static final Map<String, String> PLAYER_URLS = new HashMap<>();
    private static final List<String> OTHER_PLAYER_NAMES = new ArrayList<>();
    private static final Map<String, List<Mount>> PLAYER_MOUNTS = new HashMap<>();

    @NonNull private HttpClient httpClient;
    @NonNull private MountRepository mountRepository;
    @NonNull private PlayerRepository playerRepository;
    private String lastUpdated = "Never";

    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.TEXT_HTML_VALUE})
    public String listMountsHTML(@RequestParam(value = "refresh", required = false) String refresh,
                                 @RequestParam(value = "display", required = false) String display) throws IOException {
        if(refresh != null && refresh.equals("true")) {
            loadMounts();
        }

        SortedSet<Map.Entry<String, List<Mount>>> sortedSet = new TreeSet<>((o1, o2) -> {
            int size1 = o1.getValue().size();
            int size2 = o2.getValue().size();

            if(size1 != size2) {
                return Integer.compare(size2, size1);
            } else {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        sortedSet.addAll(PLAYER_MOUNTS.entrySet());
        Map<String, Mount> totalMounts = mountRepository.findAll().stream()
                .collect(Collectors.toMap(Mount::getInstance, mount -> mount));

        int numRows = sortedSet.size();
        int numColumns = totalMounts.values().stream()
                .mapToInt(value -> Math.toIntExact(value.getId()))
                .max()
                .orElse(0);

        Grid grid = new Grid(numRows, numColumns + 1);
        int row = 0;

        for (Map.Entry<String, List<Mount>> entry : sortedSet) {
            String playerName = entry.getKey();
            List<Mount> mounts = entry.getValue();

            grid.setValue(row, 0, playerName);

            for (Mount mount : mounts) {
                try {
                    String value = mount.getInstance();

                    if(display != null && display.equals("mountNames")) {
                        value += "<br>(" + mount.getName() + ")";
                    }

                    grid.setValue(row, Math.toIntExact(mount.getId()), value);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            row++;
        }

        StringBuilder sb = new StringBuilder();
        OTHER_PLAYER_NAMES.forEach(s -> sb
                .append("<option value=\"")
                .append(s)
                .append("\">")
                .append(s)
                .append("</option>\n"));

        return "<html><head><style>" +
                "table {border-collapse: collapse; border-style: solid;} " +
                "td, th {padding: 10px;} " +
                "td, th, p {font-family: helvetica; font-size: 14px;}" +
                "</style>" +
                "<link rel=\"stylesheet\" href=\"https://code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css\">\n" +
                "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js\"></script>\n" +
                "<script src=\"https://code.jquery.com/ui/1.12.1/jquery-ui.js\"></script>\n" +
                "<script src=\"resources/bale.js\"></script>" +
                "</head><body>" +
                "<table border=\"1\"><tr><th>Name</th><th colspan=\"" + grid.numColumnsWithValue() +
                "\">Mounts needed</th></tr>" +
                grid.toHTML() +
                "</table>" +
                "<p>Last Updated: " + lastUpdated + "</p>" +
                "<input type=\"button\" id=\"newPlayerButton\" value=\"Add Player\"/>" +
                "<div id=\"newPlayerList\" title=\"Add Player\">\n" +
                "<select name=\"player\">\n" +
                sb.toString() +
                "</select>\n" +
                "</div>" +
                "</body></html>";
    }

    @RequestMapping(value = "/json", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String listMountsJSON(@RequestParam(value = "refresh", required = false) String refresh) throws IOException {
        if(refresh != null && refresh.equals("true")) {
            loadMounts();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("mounts", PLAYER_MOUNTS);
        result.put("lastUpdated", lastUpdated);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper.writeValueAsString(result);
    }

    @RequestMapping(value = "/addPlayer", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String addPlayer(@RequestBody(required = false) String body) throws IOException {
        try {
            String playerName = URLEncodedUtils.parse(body, Charset.defaultCharset()).stream()
                    .filter(nameValuePair -> nameValuePair.getName().equals("player"))
                    .map(NameValuePair::getValue)
                    .findFirst()
                    .orElse(null);
            Player player = Player.builder()
                    .name(playerName)
                    .mounts(new ArrayList<>())
                    .build();
            playerRepository.save(player);
            loadMounts();
            OTHER_PLAYER_NAMES.remove(playerName);
            return "{\"status\":\"success\"}";
        } catch (Exception e) {
            return "{\"status\":\"error\"}";
        }
    }

    @RequestMapping(value = "/removePlayer", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String removePlayer(@RequestParam(value = "playerName") String playerName) throws IOException {
        try {
            Player player = playerRepository.findByName(playerName);

            if(player != null) {
                playerRepository.delete(player);
                loadMounts();
                return "{\"status\":\"success\"}";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{\"status\":\"error\"}";
    }

    @Scheduled(fixedRate = 3600000)
    public void loadMounts() throws IOException {
        Map<String, Mount> totalMounts = mountRepository.findAll().stream()
                .collect(Collectors.toMap(Mount::getName, mount -> mount));
        List<Player> players = playerRepository.findAll();

        loadPlayerUrls(players);

        // Load all the mounts for each player from the lodestone
        Map<Player, MountLoader> loaders = players.stream()
                .collect(Collectors.toMap(player -> player, player -> {
                    MountLoader mountLoader = new MountLoader(player, totalMounts);
                    mountLoader.start();
                    return mountLoader;
                }));

        // Wait for all loaders to finish processing
        loaders.values().forEach(mountLoader -> {
            try {
                mountLoader.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Get the list of all mounts that each player has
        Map<String, List<Mount>> mountsFromWebsite = loaders.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getName(), entry -> entry.getKey().getMounts()));

        // Convert it to a list of mounts that each player does not
        // have from the list of total mounts
        PLAYER_MOUNTS.clear();
        PLAYER_MOUNTS.putAll(mountsFromWebsite.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> totalMounts
                        .values()
                        .stream()
                        .filter(mount -> !entry.getValue().contains(mount))
                        .collect(Collectors.toList()))));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        lastUpdated = sdf.format(new Date());
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
            if(totalMounts.values().stream().allMatch(mount -> player.hasMount(mount.getName()))) {
                return;
            }

            String url = BASE_URL + PLAYER_URLS.get(player.getName());
            try {
                String content = httpClient.get(url);
                Pattern pattern = Pattern.compile("<li><div class=\"character__item_icon.+?data-tooltip=\"(.+?)\".+?</li>");
                Matcher matcher = pattern.matcher(content);
                boolean modified = false;

                while(matcher.find()) {
                    String mount = matcher.group(1);

                    if(!player.hasMount(mount) && player.addMount(totalMounts.get(mount))) {
                        modified = true;
                    }
                }

                if(modified) {
                    playerRepository.save(player);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadPlayerUrls(List<Player> players) throws IOException {
        String content = httpClient.get(BASE_URL + "/lodestone/freecompany/9229283011365743624/member/");
        Pattern pattern = Pattern.compile("<li class=\"entry\"><a href=\"(.+?)\".+?<p class=\"entry__name\">(.+?)<\\/p>.+?<\\/li>.+?<\\/li>.+?<\\/li>.+?<\\/li>");
        Matcher matcher = pattern.matcher(content);
        OTHER_PLAYER_NAMES.clear();

        while (matcher.find()) {
            String playerUrl = matcher.group(1);
            String playerName = matcher.group(2).replace("&#39;", "'");

            if(players.stream().anyMatch(player -> player.getName().equals(playerName))) {
                PLAYER_URLS.put(playerName, playerUrl);
            } else {
                OTHER_PLAYER_NAMES.add(playerName);
            }
        }
    }
}
