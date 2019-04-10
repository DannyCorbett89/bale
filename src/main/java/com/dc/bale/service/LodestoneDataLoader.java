package com.dc.bale.service;

import com.dc.bale.component.HttpClient;
import com.dc.bale.database.*;
import com.dc.bale.exception.UnableToParseNumPagesException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
    private static final String TRIALS_URL = DUTY_URL + "/?category2=4";
    public static final String ITEM_URL = DB_URL + "/item";
    private static final String MINIONS_URL = ITEM_URL + "/?category2=7&category3=81";
    private static final int REFRESH_INTERVAL = 3600000;
    private static final Object MOUNT_LOADER_LOCK = new Object();

    private final HttpClient httpClient;
    private final MountRepository mountRepository;
    private final TrialRepository trialRepository;
    private final MountIdentifierRepository mountIdentifierRepository;
    private final MinionRepository minionRepository;

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
    private void loadAllTrials() {
        loadTrials(false);
    }

    @Scheduled(cron = "0 10 * * * *")
    private void loadLatestTrials() {
        loadTrials(true);
    }

    @PostConstruct
    private void loadAllMinions() {
        loadMinions(false);
    }

    @Scheduled(cron = "0 20 * * * *")
    private void loadLatestMinions() {
        loadMinions(true);
    }

    private void loadTrials(boolean latestPatch) {
        String trialsUrl = getFullUrl(TRIALS_URL, latestPatch);
        String content = httpClient.get(trialsUrl);
        Set<String> trialNames = trialRepository.findAll().stream()
                .map(Trial::getName)
                .collect(Collectors.toSet());
        int numPages = getNumPagesDB(content, trialsUrl);

        for (int x = 1; x <= numPages; x++) {
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
                if (!trialNames.contains(trialName)) {
                    Trial trial = trialRepository.save(Trial.builder()
                            .name(trialName)
                            .lodestoneId(trialUrl)
                            .build());

                    new TrialLoader(trial).start();
                }
            }
        }
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
                    boss = "TODO: Fix name";
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
                                    .visible(true)
                                    .build());
                        }

                        mounts.add(mount);
                    }
                }

                if (!mounts.isEmpty()) {
                    trial.setMounts(mounts);
                }

                Pattern ilvlPattern = Pattern.compile("<li>Avg\\. Item Level: (.+?)</li>");
                Matcher ilvlMatcher = ilvlPattern.matcher(content);

                if (ilvlMatcher.find()) {
                    String ilvlString = ilvlMatcher.group(1);
                    int ilvl = Integer.parseInt(ilvlString);
                    trial.setItemLevel(ilvl);
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

    private int getNumPagesDB(String content, String url) {
        String urlPrefix = url.contains("&") ? url.substring(0, url.indexOf("&")) : url;
        String searchTerm = urlPrefix + "&page=";
        String formattedSearchTerm = searchTerm.replace("&", "&amp;");
        int total = StringUtils.countMatches(content, formattedSearchTerm);

        if (total <= 2) {
            return 1;
        }

        Pattern pattern = Pattern.compile("<li class=\"next_all\"><a href=\"" + formattedSearchTerm.replace("?", "\\?") + "([0-9]+).+?</li>");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String numPages = matcher.group(1);
            return Integer.parseInt(numPages);
        }

        throw new UnableToParseNumPagesException();
    }

    private String getFullUrl(String url, boolean latestPatch) {
        if (latestPatch) {
            url += "&patch=latest";
        }

        return url;
    }

    private void loadMinions(boolean latestPatch) {
        // TODO: Refactor, extract
        String minionsUrl = getFullUrl(MINIONS_URL, latestPatch);
        String content = httpClient.get(minionsUrl);
        Set<String> minionNames = minionRepository.findAll().stream()
                .map(Minion::getName)
                .collect(Collectors.toSet());
        int numPages = getNumPagesDB(content, minionsUrl);

        for (int x = 1; x <= numPages; x++) {
            if (x > 1) {
                content = httpClient.get(minionsUrl + "&page=" + x)
                        .replace("\n", "")
                        .replace("\r", "");
            }

            Pattern pattern = Pattern.compile("</span>.+?<a href=\"/lodestone/playguide/db/item/([a-z0-9]+)/(?:\\?patch=latest)?\" class=\"db_popup db-table__txt--detail_link\">(.+?)</a>");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                String lodestoneId = matcher.group(1);
                String name = matcher.group(2)
                        .replace("<i>", "")
                        .replace("</i>", "");
                if (!minionNames.contains(name)) {
                    Minion minion = minionRepository.save(Minion.builder()
                            .name(name)
                            .lodestoneId(lodestoneId)
                            .build());
                    log.info("Loaded " + minion.getName());
                }
            }
        }
    }
}
