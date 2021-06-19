package com.dc.bale.service;

import com.dc.bale.database.dao.MountItemRepository;
import com.dc.bale.database.entity.MountItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MountItemService {
    private final MountItemRepository mountItemRepository;

    synchronized void addMountItem(String itemName, String mountName) {
        MountItem mountItem = mountItemRepository.findByItemName(itemName);

        if (mountItem == null) {
            mountItemRepository.save(MountItem.builder()
                    .itemName(itemName)
                    .mountName(mountName)
                    .build());
        }
    }
}