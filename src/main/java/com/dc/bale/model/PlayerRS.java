package com.dc.bale.model;

import com.dc.bale.database.Mount;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlayerRS {
    private String name;
    private List<Mount> mounts;

    public long numMounts() {
        return mounts.stream().filter(mount -> mount.getName() != null && mount.getInstance() != null).count();
    }
}
