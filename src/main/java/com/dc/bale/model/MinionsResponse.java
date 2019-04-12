package com.dc.bale.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MinionsResponse {
    private String lastUpdated;
    private List<MinionRS> players;
    private List<Column> columns;

    public int getNumRows() {
        return players.size();
    }
}
