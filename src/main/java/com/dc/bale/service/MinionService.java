package com.dc.bale.service;

import com.dc.bale.database.dao.MinionRepository;
import com.dc.bale.database.dao.PlayerRepository;
import com.dc.bale.database.entity.Minion;
import com.dc.bale.database.entity.Player;
import com.dc.bale.model.Column;
import com.dc.bale.model.MinionRS;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MinionService {
    private static final int WIDTH_MODIFIER = 11;
    private final MinionRepository minionRepository;
    private final PlayerRepository playerRepository;

    public List<MinionRS> getMinions() {
        List<Player> visiblePlayers = playerRepository.findByVisibleTrue();
        List<Minion> totalMinions = minionRepository.findAll();
        return totalMinions.stream()
                .filter(minion -> anyPlayerNeedsMinion(minion, visiblePlayers))
                .map(minion -> getPlayersNeedingMinion(visiblePlayers, minion))
                .sorted(Comparator.comparing(MinionRS::getName))
                .collect(Collectors.toList());
    }

    public List<Column> getColumns() {
        List<Player> visiblePlayers = playerRepository.findByVisibleTrue();
        List<Column> columns = new ArrayList<>();
        columns.add(Column.builder()
                .key("name")
                .name("Minion Name")
                .frozen(true)
                .width(200)
                .build());
        columns.addAll(visiblePlayers.stream()
                .map(this::getColumn)
                .sorted(Comparator.comparing(Column::getName))
                .collect(Collectors.toList()));
        return columns;
    }

    private boolean anyPlayerNeedsMinion(Minion minion, List<Player> players) {
        return players.stream().anyMatch(player -> !player.hasMinion(minion.getName()));
    }

    private MinionRS getPlayersNeedingMinion(List<Player> players, Minion minion) {
        return MinionRS.builder()
                .id(minion.getId())
                .name(minion.getDisplayName())
                .lodestoneId(minion.getLodestoneId())
                .players(players.stream()
                        .filter(player1 -> playerDoesNotHaveMinion(minion, player1))
                        .collect(Collectors.toList()).stream()
                        .collect(Collectors.toMap(player -> "player-" + player.getId(), player -> "X")))
                .build();
    }

    private boolean playerDoesNotHaveMinion(Minion minion, Player player1) {
        return player1.getMinions().stream()
                .noneMatch(playerMinion -> playerMinion.getId() == minion.getId());
    }

    private Column getColumn(Player player) {
        return Column.builder()
                .key("player-" + player.getId())
                .name(player.getName())
                .width(player.getName().length() * WIDTH_MODIFIER)
                .build();
    }
}
