package com.dc.bale.database.dao;

import com.dc.bale.database.entity.MountLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MountLinkRepository extends JpaRepository<MountLink, Long> {
    List<MountLink> findAllByMountIdAndTrialIdGreaterThan(long mountId, long trialId);
}
