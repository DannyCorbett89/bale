package com.dc.bale.service;

import com.dc.bale.component.HttpClient;
import com.dc.bale.component.JsonConverter;
import com.dc.bale.database.MountRepository;
import com.dc.bale.database.Trial;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class XivdbService {
    private static final Map<String, Long> INSTANCE_IDS = new HashMap<>();
    private static final String XIVDB_BASE_URL = "https://api.xivdb.com";

    @NonNull
    private HttpClient httpClient;
    @NonNull
    private MountRepository mountRepository;
    @NonNull
    private JsonConverter jsonConverter;

    private Map<String, Long> xivdbIds = new HashMap<>();

    // TODO: Scrape raids as well as trials. Then remove this
    public void loadXivDBIds() throws IOException {
        // TODO: Endpoints are dead
//        String content = httpClient.get(XIVDB_BASE_URL + "/mount");
//        List<Map<String, Object>> mounts = jsonConverter.toObject(content);
//        mounts.forEach(mount -> xivdbIds.put((String) mount.get("name"), ((Integer) mount.get("id")).longValue()));
//
//        List<String> existingMounts = mountRepository.findAll().stream()
//                .map(Mount::getName)
//                .collect(Collectors.toList());
//
//        List<Mount> newMounts = xivdbIds.entrySet().stream()
//                .filter(entry -> !existingMounts.contains(entry.getKey()))
//                .map(entry -> Mount.builder()
//                        .name(entry.getKey())
//                        .build())
//                .collect(Collectors.toList());
//
//        mountRepository.save(newMounts);
    }

    public String getTrialBossName(Trial trial) throws IOException {
        // TODO: Endpoints are dead
//        if (INSTANCE_IDS.isEmpty()) {
//            String xivdbContent = httpClient.get(XIVDB_BASE_URL + "/instance");
//            List<Map<String, Object>> instances = jsonConverter.toObject(xivdbContent);
//            instances.forEach(mount -> INSTANCE_IDS.put((String) mount.get("name"), ((Integer) mount.get("id")).longValue()));
//        }
//
//        String xivdbContent = httpClient.get(XIVDB_BASE_URL + "/instance/" + INSTANCE_IDS.get(trial.getName()));
//        XivdbResponse trialInfo = jsonConverter.toObject(xivdbContent, XivdbResponse.class);
//        List<Enemy> enemies = trialInfo.getEnemies();
//
//        if (!enemies.isEmpty()) {
//            Enemy enemy = enemies.get(0);
//            return enemy.getName();
//        }
//
//        return null;
        return "TODO: Fix name";
    }
}
