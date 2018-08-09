package com.dc.bale.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Response {
    private String lastUpdated;
    private List<PlayerRS> players;

    public int getColumns() {
        return players.stream()
                .reduce((player, player2) -> player.getMounts().size() > player2.getMounts().size() ? player : player2)
                .map(playerRS -> playerRS.getMounts().size() + 1)
                .orElse(1);
    }
}
