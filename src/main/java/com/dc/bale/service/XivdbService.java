package com.dc.bale.service;

import com.dc.bale.component.HttpClient;
import com.dc.bale.component.JsonConverter;
import com.dc.bale.database.Mount;
import com.dc.bale.database.MountRepository;
import com.dc.bale.database.Trial;
import com.dc.bale.model.Enemy;
import com.dc.bale.model.XivdbResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Optional<String> response = httpClient.get(XIVDB_BASE_URL + "/mount");
        if (!response.isPresent()) {
            return;
        }
        String content = response.get();
        List<Map<String, Object>> mounts = jsonConverter.toObject(content);
        mounts.forEach(mount -> xivdbIds.put((String) mount.get("name"), ((Integer) mount.get("id")).longValue()));

        List<String> existingMounts = mountRepository.findAll().stream()
                .map(Mount::getName)
                .collect(Collectors.toList());

        List<Mount> newMounts = xivdbIds.entrySet().stream()
                .filter(entry -> !existingMounts.contains(entry.getKey()))
                .map(entry -> Mount.builder()
                        .name(entry.getKey())
                        .build())
                .collect(Collectors.toList());

        mountRepository.save(newMounts);
    }

    public String getTrialBossName(Trial trial) throws IOException {
        if (INSTANCE_IDS.isEmpty()) {
            Optional<String> xivdbResponse = httpClient.get(XIVDB_BASE_URL + "/instance");
            if (xivdbResponse.isPresent()) {
                String xivdbContent = xivdbResponse.get();
                List<Map<String, Object>> instances = jsonConverter.toObject(xivdbContent);
                instances.forEach(mount -> INSTANCE_IDS.put((String) mount.get("name"), ((Integer) mount.get("id")).longValue()));
            }
        }

        Optional<String> xivdbResponse = httpClient.get(XIVDB_BASE_URL + "/instance/" + INSTANCE_IDS.get(trial.getName()));
        if (xivdbResponse.isPresent()) {
            String xivdbContent = xivdbResponse.get();
            XivdbResponse trialInfo = jsonConverter.toObject(xivdbContent, XivdbResponse.class);
            List<Enemy> enemies = trialInfo.getEnemies();

            if (!enemies.isEmpty()) {
                Enemy enemy = enemies.get(0);
                return enemy.getName();
            }
        }

        return null;
    }
}
