package com.dc.bale.database;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FcRankRepository extends JpaRepository<FcRank, Long> {
    FcRank findByIcon(String icon);

    List<FcRank> findAllByEnabledTrue();
}
