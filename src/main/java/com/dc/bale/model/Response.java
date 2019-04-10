package com.dc.bale.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Response {
    private String lastUpdated;
    private List<PlayerRS> players;
    private List<Column> columns;

    public int getNumColumns() {
        if (players.stream().allMatch(player -> player.getMounts() != null)) {
            return players.stream()
                    .reduce((player, player2) -> player.getMounts().size() > player2.getMounts().size() ? player : player2)
                    .map(playerRS -> playerRS.getMounts().size() + 1)
                    .orElse(1);
        } else if (players.stream().allMatch(player -> player.getMinions() != null)) {
            return players.stream()
                    .reduce((player, player2) -> player.getMinions().size() > player2.getMinions().size() ? player : player2)
                    .map(playerRS -> playerRS.getMinions().size() + 1)
                    .orElse(1);
        } else {
            return 0;
        }
    }

    public int getNumRows() {
        return players.size();
    }
}
