package com.dc.bale.database;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MountRepository extends JpaRepository<Mount, Long> {
    boolean existsByName(String name);

    boolean existsByInstance(String instance);
}