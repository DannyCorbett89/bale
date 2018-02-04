package com.dc.bale.database;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicKeyMappingRepository extends JpaRepository<MusicKeyMapping, Long> {
    MusicKeyMapping findByName(String name);
}