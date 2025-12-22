package com.dc.bale.service;

import com.dc.bale.database.entity.Mount;
import com.dc.bale.database.entity.Source;
import com.dc.bale.model.ffxivcollect.MinionsRS;
import com.dc.bale.model.ffxivcollect.MountsRS;
import com.dc.bale.util.SourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class FFXIVCollectDataLoader {
    private final FFXIVCollectAPI ffxivCollectAPI;
    private final MountService mountService;
    private final MinionService minionService;
    private final InstanceService instanceService;
    private final PlayerTracker playerTracker;

    @PostConstruct
    @Scheduled(cron = "0 10 * * * *")
    private void loadMounts() {
        log.info("Loading mounts...");
        loadTrials();
        loadRaids();
        log.info("Finished loading mounts");
        playerTracker.loadMounts();
    }

    @PostConstruct
    @Scheduled(cron = "0 10 * * * *")
    private void loadMinions() {
        log.info("Loading minions...");
        MinionsRS minions = ffxivCollectAPI.getMinions();
        minions.getResults()
                .forEach(minion -> minionService.addMinion(minion.getId(), minion.getName()));
        log.info("Finished loading minions");
        playerTracker.loadMinions();
    }

    private void loadTrials() {
        MountsRS mounts = ffxivCollectAPI.getMounts(SourceType.TRIALS);
        storeMounts(mounts);
    }

    private void loadRaids() {
        MountsRS mounts = ffxivCollectAPI.getMounts(SourceType.RAIDS);
        storeMounts(mounts);
    }

    private void storeMounts(MountsRS mounts) {
        mounts.getResults()
                .forEach(ffxivCollectMount -> {
                    Mount mount = mountService.addMount(ffxivCollectMount.getId(), ffxivCollectMount.getName());
                    ffxivCollectMount.getSources()
                            .stream()
                            .filter(source -> source.getRelatedType() != null)
                            .filter(source -> source.getText() != null)
                            .filter(this::isValidName)
                            .forEach(source -> instanceService.addInstance(source.getRelatedId(), source.getType(), source.getText(), mount));
                });
    }

    private boolean isValidName(Source source) {
        String name = source.getText().toLowerCase();
        boolean valid = name.contains("extreme") || name.contains("minstrel") || name.contains("savage");

        if(!valid) {
            log.info("Skipping invalid mount source: {}", source.getText());
        }

        return valid;
    }
}
