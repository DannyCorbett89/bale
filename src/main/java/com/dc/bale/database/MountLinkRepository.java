package com.dc.bale.database;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MountLinkRepository extends JpaRepository<MountLink, Long> {
    void deleteByMountId(Long mountId);
}
