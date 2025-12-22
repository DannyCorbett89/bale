package com.dc.bale.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MinionsRS {
    private List<PlayerRS> players;
    private List<MinionRS> minions;
    private String lastUpdated;
}
