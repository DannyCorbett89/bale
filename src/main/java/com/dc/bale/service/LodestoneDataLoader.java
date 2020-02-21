package com.dc.bale.service;

import com.dc.bale.Constants;
import com.dc.bale.component.HttpClient;
import com.dc.bale.database.dao.MinionRepository;
import com.dc.bale.database.dao.MountLinkRepository;
import com.dc.bale.database.dao.TrialRepository;
import com.dc.bale.database.entity.Minion;
import com.dc.bale.database.entity.Mount;
import com.dc.bale.database.entity.MountLink;
import com.dc.bale.database.entity.Trial;
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

import static com.dc.bale.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LodestoneDataLoader {

    private final HttpClient httpClient;
    private final MountService mountService;
    private final TrialRepository trialRepository;
    private final MinionRepository minionRepository;
    private final PlayerTracker playerTracker;
    private final MountItemService mountItemService;
    private final MountLinkRepository mountLinkRepository;
    private final ConfigService configService;

    @PostConstruct
    private void loadAllTrials() {
        loadTrials(false);
        playerTracker.loadMounts();
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
        List<Trial> trials = trialRepository.findAll();
        Set<String> trialNames = trials.stream()
                .map(Trial::getName)
                .collect(Collectors.toSet());
        int numPages = getNumPagesDB(content, trialsUrl);

        for (int x = 1; x <= numPages; x++) {
            if (x > 1) {
                content = httpClient.get(trialsUrl + "&page=" + x);
            }

            String trialsRegex = configService.getConfig("regex_trials");
            Pattern pattern = Pattern.compile(trialsRegex);
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
                String trialBossNameRegex = configService.getConfig("regex_trial_boss_name");
                Pattern pattern = Pattern.compile(trialBossNameRegex, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(content);
                Trial oldValues = trial.toBuilder().build();
                String boss;

                if (matcher.find()) {
                    boss = matcher.group(1);
                } else {
                    boss = "TODO: Fix name";
                }
                trial.setBoss(boss);

                String itemRegex = configService.getConfig("regex_trial_item");
                Pattern itemPattern = Pattern.compile(itemRegex);
                Matcher itemMatcher = itemPattern.matcher(content);
                List<Mount> mounts = new ArrayList<>();

                while (itemMatcher.find()) {
                    String itemId = itemMatcher.group(1);
                    String item = itemMatcher.group(2);

                    // TODO: Make this multithreaded

                    String itemContent = httpClient.get(Constants.ITEM_URL + "/" + itemId)
                            .replaceAll("\n", "")
                            .replaceAll("\r", "");

                    if (itemContent.contains("Use to Acquire") && itemContent.contains("<li><a href=\"/lodestone/playguide/db/item/?category2=7&amp;category3=63\" ")) {
                        String useToAcquireRegex = configService.getConfig("regex_trial_use_to_acquire");
                        Pattern mountPattern = Pattern.compile(useToAcquireRegex);
                        Matcher mountMatcher = mountPattern.matcher(itemContent);

                        if (mountMatcher.find()) {
                            String mountName = mountMatcher.group(1);
                            log.info(item + " -> " + mountName);
                            Mount mount = mountService.addMount(mountName);
                            mounts.add(mount);

                            mountItemService.addMountItem(item, mountName);
                            mountLinkRepository.save(MountLink.builder()
                                    .mountId(mount.getId())
                                    .trialId(trial.getId())
                                    .build());
                        }
                    }
                }

                if (!mounts.isEmpty()) {
                    trial.setMounts(mounts);
                }

                String ilevelRegex = configService.getConfig("regex_trial_ilevel");
                Pattern ilvlPattern = Pattern.compile(ilevelRegex);
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

            String minionsRegex = configService.getConfig("regex_all_minions");
            Pattern pattern = Pattern.compile(minionsRegex);
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
