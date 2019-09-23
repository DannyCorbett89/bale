package com.dc.bale.database.dao;

import com.dc.bale.database.entity.MountItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MountItemRepository extends JpaRepository<MountItem, Long> {
    MountItem findByItemName(String itemName);
}
