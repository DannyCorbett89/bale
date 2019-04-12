package com.dc.bale.database.dao;

import com.dc.bale.database.entity.Minion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MinionRepository extends JpaRepository<Minion, Long> {
}
