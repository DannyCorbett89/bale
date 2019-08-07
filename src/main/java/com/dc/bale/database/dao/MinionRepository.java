package com.dc.bale.database.dao;

import com.dc.bale.database.entity.Minion;
import com.dc.bale.database.entity.Mount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MinionRepository extends JpaRepository<Minion, Long> {
    Minion findByName(String name);

    Minion findByHash(String hash);
}
