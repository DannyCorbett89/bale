package com.dc.bale.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MountsRS {
    private List<PlayerRS> players;
    private List<MountRS> mounts;
    private String lastUpdated;
}
