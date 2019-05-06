package com.dc.bale.service;

import com.dc.bale.database.dao.MountLinkRepository;
import com.dc.bale.database.dao.TrialRepository;
import com.dc.bale.database.entity.Mount;
import com.dc.bale.database.entity.MountLink;
import com.dc.bale.database.entity.Trial;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TrialService {
    private final TrialRepository trialRepository;
    private final MountLinkRepository mountLinkRepository;

    String getInstance(Mount mount) {
        if (mount.getName() == null) {
            return null;
        }

        List<String> trialBossNames = mountLinkRepository.findAllByMountIdAndTrialIdGreaterThan(mount.getId(), 0).stream()
                .map(mountLink -> trialRepository.findOne(mountLink.getTrialId()).getBoss())
                .collect(Collectors.toList());

        if (trialBossNames.size() != 1) {
            return mount.getName();
        } else {
            return StringUtils.join(trialBossNames, "/");
        }
    }

    Map<Long, Long> getMountItemLevels() {
        return mountLinkRepository.findAll().stream()
                .filter(mountLink -> mountLink.getTrialId() > 0)
                .collect(Collectors.toMap(
                        MountLink::getMountId,
                        mountLink -> {
                            Trial trial = trialRepository.findOne(mountLink.getTrialId());
                            if (trial != null) {
                                return trial.getItemLevel();
                            } else {
                                return 0L;
                            }
                        },
                        (first, second) -> first)
                );
    }
}
