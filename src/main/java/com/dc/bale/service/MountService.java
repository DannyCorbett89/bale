package com.dc.bale.service;

import com.dc.bale.database.dao.MountRepository;
import com.dc.bale.database.entity.Mount;
import com.dc.bale.database.entity.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class MountService {
    private final MountRepository mountRepository;

    synchronized Optional<Mount> getAndUpdateMountForHash(Player player, String hash, Supplier<String> lookupMountNameFromHash) {
        Mount mount = mountRepository.findByHash(hash);
        if (mount != null) {
            return Optional.of(mount);
        } else {
            String name = lookupMountNameFromHash.get();

            if (name != null) {
                Mount mountByName = mountRepository.findByName(name);

                if (mountByName != null) {
                    mountByName.setHash(hash);
                    mountRepository.save(mountByName);
                } else {
                    mountByName = Mount.builder()
                            .name(name)
                            .hash(hash)
                            .visible(false)
                            .build();
                    mountRepository.save(mountByName);
                }

                return Optional.of(mountByName);
            } else {
                log.error("Unable to load mount name for hash {}, player {}", hash, player.getName());
                return Optional.empty();
            }
        }
    }

    synchronized Mount addMount(String name) {
        Mount mount = mountRepository.findByName(name);

        if (mount == null) {
            mount = mountRepository.save(Mount.builder()
                    .name(name)
                    .visible(true)
                    .build());
        }

        return mount;
    }
}
