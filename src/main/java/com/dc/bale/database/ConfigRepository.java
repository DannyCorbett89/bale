package com.dc.bale.database;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<Config, Long> {
    Config findByName(String name);
}