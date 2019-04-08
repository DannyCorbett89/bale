package com.dc.bale.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@Builder
public class PlayerRS {
    private long id;
    private String name;
    private List<MountRS> mounts;
    @JsonIgnore
    private Map<String, String> minions;

    public long numMounts() {
        return mounts.stream().filter(mount -> mount.getName() != null && mount.getInstance() != null).count();
    }

    public long numMinions() {
        return minions.values().stream().filter(Objects::nonNull).count();
    }

    @JsonAnyGetter
    public Map<String, String> getMinions() {
        return minions;
    }
}
