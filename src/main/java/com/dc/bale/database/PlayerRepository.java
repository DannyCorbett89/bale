package com.dc.bale.database;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByName(String name);

    List<Player> findByNameIn(Set<String> names);

    List<Player> findByTrackingFalseAndRankIdIsInOrderByName(List<Long> rankIcons);
}
