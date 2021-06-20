package com.dc.bale.service;

import com.dc.bale.database.dao.InstanceRepository;
import com.dc.bale.database.dao.MountSourceLinkRepository;
import com.dc.bale.database.entity.Instance;
import com.dc.bale.database.entity.Mount;
import com.dc.bale.database.entity.MountSourceLink;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstanceService {
    private final InstanceRepository instanceRepository;
    private final MountSourceLinkRepository mountSourceLinkRepository;

    public void addInstance(long id, String type, String name, Mount mount) {
        Instance instance = instanceRepository.findOne(id);

        if (instance == null) {
            instance = instanceRepository.save(Instance.builder()
                    .id(id)
                    .type(type)
                    .name(name)
                    .build());
        }

        MountSourceLink link = mountSourceLinkRepository.findByMountIdAndInstanceId(mount.getId(), instance.getId());

        if (link == null) {
            mountSourceLinkRepository.save(MountSourceLink.builder()
                    .mountId(mount.getId())
                    .instanceId(instance.getId())
                    .build());
        }
    }

    public Map<Long, Instance> getInstances(List<Mount> totalMounts) {
        List<Long> mountIds = totalMounts.stream()
                .map(Mount::getId)
                .collect(Collectors.toList());
        Map<Long, Instance> instances = instanceRepository.findAll().stream().collect(Collectors.toMap(Instance::getId, instance -> instance));

        return mountSourceLinkRepository.findByMountIdIn(mountIds)
                .stream()
                .collect(Collectors.toMap(
                        MountSourceLink::getMountId,
                        mountSourceLink -> instances.get(mountSourceLink.getInstanceId()),
                        (instance1, instance2) -> instance1
                ));
    }
}
