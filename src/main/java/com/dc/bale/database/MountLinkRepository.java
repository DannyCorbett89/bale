package com.dc.bale.database;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MountLinkRepository extends JpaRepository<MountLink, Long> {
    List<MountLink> findAllByMountIdAndTrialIdGreaterThan(long mountId, long trialId);

    void deleteByMountId(Long mountId);
}
