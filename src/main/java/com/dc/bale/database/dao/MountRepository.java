package com.dc.bale.database.dao;

import com.dc.bale.database.entity.Mount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MountRepository extends JpaRepository<Mount, Long> {
    Mount findByName(String name);

    List<Mount> findAllByVisible(boolean visible);
}