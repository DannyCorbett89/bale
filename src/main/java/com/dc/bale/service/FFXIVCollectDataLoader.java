package com.dc.bale.service;

import com.dc.bale.database.entity.Mount;
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
    private final InstanceService instanceService;
    private final PlayerTracker playerTracker;

    @PostConstruct
    @Scheduled(cron = "0 10 * * * *")
    private void load() {
        loadTrials();
        loadRaids();
        playerTracker.loadMounts();
    }

    private void loadTrials() {
        MountsRS response = ffxivCollectAPI.getMounts(SourceType.TRIALS);
        storeMounts(response);
    }

    private void loadRaids() {
        MountsRS response = ffxivCollectAPI.getMounts(SourceType.RAIDS);
        storeMounts(response);
    }

    private void storeMounts(MountsRS response) {
        response.getResults()
                .forEach(ffxivCollectMount -> {
                    Mount mount = mountService.addMount(ffxivCollectMount.getName());
                    ffxivCollectMount.getSources()
                            .forEach(source -> instanceService.addInstance(source.getRelatedId(), source.getType(), source.getText(), mount));
                });
    }
}
