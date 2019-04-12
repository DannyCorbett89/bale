package com.dc.bale.controller;

import com.dc.bale.component.JsonConverter;
import com.dc.bale.database.dao.MinionRepository;
import com.dc.bale.database.dao.PlayerRepository;
import com.dc.bale.database.entity.Minion;
import com.dc.bale.database.entity.Player;
import com.dc.bale.model.Column;
import com.dc.bale.model.MinionRS;
import com.dc.bale.model.MinionsResponse;
import com.dc.bale.service.PlayerTracker;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/minions")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MinionController {
    private static final int WIDTH_MODIFIER = 11;
    private final JsonConverter jsonConverter;
    private final PlayerTracker playerTracker;
    private final MinionRepository minionRepository;
    private final PlayerRepository playerRepository;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> listMinions(@RequestParam(value = "refresh", required = false) String refresh) {
        if (refresh != null && refresh.equals("true")) {
            playerTracker.loadMounts();
        }

        List<Player> visiblePlayers = playerRepository.findByVisibleTrue();
        List<Minion> totalMinions = minionRepository.findAll();
        List<MinionRS> minions = totalMinions.stream()
                .filter(minion -> anyPlayerNeedsMinion(minion, visiblePlayers))
                .map(minion -> getPlayersNeedingMinion(visiblePlayers, minion))
                .sorted(Comparator.comparing(MinionRS::getName))
                .collect(Collectors.toList());

        List<Column> columns = new ArrayList<>();
        columns.add(Column.builder()
                .key("name")
                .name("Minion Name")
                .frozen(true)
                .width(170)
                .build());
        columns.addAll(visiblePlayers.stream()
                .map(this::getColumn)
                .sorted(Comparator.comparing(Column::getName))
                .collect(Collectors.toList()));

        MinionsResponse response = MinionsResponse.builder()
                .lastUpdated(playerTracker.getLastUpdated())
                .columns(columns)
                .players(minions)
                .build();

        return toResponse(response);
    }

    private boolean anyPlayerNeedsMinion(Minion minion, List<Player> players) {
        return players.stream().anyMatch(player -> !player.hasMinion(minion.getName()));
    }

    private MinionRS getPlayersNeedingMinion(List<Player> players, Minion minion) {
        return MinionRS.builder()
                .id(minion.getId())
                .name(minion.getDisplayName())
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

    private ResponseEntity<String> toResponse(Object json) {
        String message;

        try {
            message = jsonConverter.toString(json);
        } catch (JsonProcessingException e) {
            message = e.getMessage();
        }

        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .body(message);
    }
}
