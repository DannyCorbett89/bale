package com.dc.bale.service;

import com.dc.bale.component.HttpClient;
import com.dc.bale.database.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LodestoneDataLoader {
    private static final String BASE_URL = "https://na.finalfantasyxiv.com/lodestone";
    private static final String DB_URL = BASE_URL + "/playguide/db";
    private static final String DUTY_URL = DB_URL + "/duty";
    private static final int REFRESH_INTERVAL = 3600000;
    private static final Object MOUNT_LOADER_LOCK = new Object();

    @NonNull
    private HttpClient httpClient;
    @NonNull
    private MountRepository mountRepository;
    @NonNull
    private TrialRepository trialRepository;
    @NonNull
    private MountIdentifierRepository mountIdentifierRepository;
    @NonNull
    private XivdbService xivdbService;

    private String mountIdentifiersRegex;

    @PostConstruct
    @Scheduled(fixedRate = REFRESH_INTERVAL)
    private void updateRegex() {
        List<String> mountIdentifiers = mountIdentifierRepository.findAll().stream()
                .map(MountIdentifier::getName)
                .collect(Collectors.toList());
        mountIdentifiersRegex = "(" + StringUtils.join(mountIdentifiers, "|") + ")";
    }

    @PostConstruct
    private void loadAllTrials() throws IOException {
        loadTrials(false);
    }

    @Scheduled(cron = "0 10 * * * *")
    private void loadLatestTrials() throws IOException {
        loadTrials(true);
    }

    private void loadTrials(boolean latestPatch) throws IOException {
        log.info("Loading " + (latestPatch ? "latest" : "all") + " trials");
        String trialsUrl = DUTY_URL + "/?category2=4";

        if (latestPatch) {
            trialsUrl += "&patch=latest";
        }

        String content = httpClient.get(trialsUrl);
        Set<String> trialNames = trialRepository.findAll().stream()
                .map(Trial::getName)
                .collect(Collectors.toSet());
        int numPages = getNumPagesDB(content, trialsUrl);
        List<TrialLoader> loaders = new ArrayList<>();

        for (int x = 1; x <= numPages; x++) {
            // First page is already loaded, don't load it again
            if (x > 1) {
                content = httpClient.get(trialsUrl + "&page=" + x);
            }

            Pattern pattern = Pattern.compile("<a href=\"/lodestone/playguide/db/duty/(.+?)/.+?>(?:(.+? \\(Extreme\\)|(The Minstrel's Ballad: .+?)))</a>");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                String trialUrl = matcher.group(1);
                String trialName = matcher.group(2)
                        .replace("<i>", "")
                        .replace("</i>", "");
                // TODO: Translation table to match mount name with whistle name
                // TODO: Store regex in configs table, in case it changes on lodestone
                if (!trialNames.contains(trialName)) {
                    Trial trial = trialRepository.save(Trial.builder()
                            .name(trialName)
                            .lodestoneId(trialUrl)
                            .build());

                    TrialLoader loader = new TrialLoader(trial);
                    loaders.add(loader);
                    loader.start();
                }
            }
        }

        for (TrialLoader loader : loaders) {
            try {
                loader.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        xivdbService.loadXivDBIds();
    }

    public class TrialLoader extends Thread {
        private Trial trial;

        TrialLoader(Trial trial) {
            super("TrialLoader-" + trial.getName().replace(" ", "_"));
            this.trial = trial;
        }

        @Override
        public void run() {
            String url = DUTY_URL + "/" + trial.getLodestoneId();
            try {
                String content = httpClient.get(url);
                Pattern pattern = Pattern.compile("<li class=\"boss.+?class=\"db_popup\"><strong>(.+?)</strong>", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(content);
                Trial oldValues = trial.toBuilder().build();
                String boss;

                if (matcher.find()) {
                    boss = matcher.group(1);
                } else {
                    boss = xivdbService.getTrialBossName(trial);
                }
                trial.setBoss(boss);

                Pattern mountPattern = Pattern.compile("<a href=\"/lodestone/playguide/db/item/.+?>(.+?) " + mountIdentifiersRegex + "</a>");
                Matcher mountMatcher = mountPattern.matcher(content);
                List<Mount> mounts = new ArrayList<>();

                while (mountMatcher.find()) {
                    String mountName = mountMatcher.group(1);

                    synchronized (MOUNT_LOADER_LOCK) {
                        Mount mount = mountRepository.findByName(mountName);

                        if (mount == null) {
                            mount = mountRepository.save(Mount.builder()
                                    .name(mountName)
                                    .tracking(true)
                                    .build());
                        }

                        mounts.add(mount);
                    }
                }

                if (!mounts.isEmpty()) {
                    trial.setMounts(mounts);
                }

                if (trial.hasAllValues()) {
                    trial.setLoaded(true);
                }

                if (!trial.equals(oldValues)) {
                    trialRepository.save(trial);
                }

                log.info("Loaded " + trial.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // TODO: Interface/Abstract class for data loader service?
    private int getNumPagesDB(String content, String url) {
        String searchTerm = url + "&page=";
        String formattedSearchTerm = searchTerm.replace("&", "&amp;");
        int total = StringUtils.countMatches(content, formattedSearchTerm);

        if (total == 0) {
            return 1;
        }

        int singlePager = total / 2;
        return singlePager - 2;
    }
}
