package com.dc.bale.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Response {
    private String lastUpdated;
    private List<MountRS> players;
    private List<Column> columns;

    public int getNumRows() {
        return players.size();
    }
}
