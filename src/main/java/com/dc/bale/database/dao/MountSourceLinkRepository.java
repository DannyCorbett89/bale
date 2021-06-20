package com.dc.bale.database.dao;

import com.dc.bale.database.entity.MountSourceLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MountSourceLinkRepository extends JpaRepository<MountSourceLink, Long> {
    MountSourceLink findByMountIdAndInstanceId(long mountId, long instanceId);

    List<MountSourceLink> findByMountIdIn(List<Long> mountIds);
}
