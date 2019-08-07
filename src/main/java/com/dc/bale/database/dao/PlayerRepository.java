package com.dc.bale.database.dao;

import com.dc.bale.database.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByName(String name);

    List<Player> findByVisibleTrue();

    List<Player> findByVisibleFalseOrderByName();

    List<Player> findByIdIsIn(List<Long> ids);
}
