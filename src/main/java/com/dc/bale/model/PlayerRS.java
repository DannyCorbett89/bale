package com.dc.bale.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlayerRS {
    private String name;
    private List<MountRS> mounts;

    public long numMounts() {
        return mounts.stream().filter(mount -> mount.getName() != null && mount.getInstance() != null).count();
    }
}
