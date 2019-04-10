package com.dc.bale.database;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MountRepository extends JpaRepository<Mount, Long> {
    Mount findByName(String name);

    List<Mount> findAllByVisible(boolean visible);
}