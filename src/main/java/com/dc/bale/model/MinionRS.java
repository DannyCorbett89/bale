package com.dc.bale.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MinionRS {
    private long id;
    private String name;
    private List<PlayerV2RS> players;
}
