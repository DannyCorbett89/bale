package com.dc.bale.database.dao;

import com.dc.bale.database.entity.Config;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigRepository extends JpaRepository<Config, Long> {
    Optional<Config> findByName(String name);
}