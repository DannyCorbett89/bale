package com.dc.bale.controller;

import com.dc.bale.component.JsonConverter;
import com.dc.bale.model.Column;
import com.dc.bale.model.PlayerRS;
import com.dc.bale.model.Response;
import com.dc.bale.service.MountTracker;
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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/minions")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MinionController {
    private static final int WIDTH_MODIFIER = 11;
    private final JsonConverter jsonConverter;
    private final MountTracker mountTracker;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> listMinions(@RequestParam(value = "refresh", required = false) String refresh) {
        if (refresh != null && refresh.equals("true")) {
            mountTracker.loadMounts();
        }

        List<PlayerRS> minions = mountTracker.getMinions();

        List<Column> columns = new ArrayList<>();
        columns.add(Column.builder().key("name").name("Player Name").width(getLongestPlayerName(minions) * WIDTH_MODIFIER).frozen(true).build());
        columns.addAll(minions.get(0).getMinions().entrySet().stream().map(entry -> Column.builder()
                .key(entry.getKey())
                .width(entry.getValue().length() * WIDTH_MODIFIER)
                .frozen(false)
                .build()).collect(Collectors.toSet()));

        Response response = Response.builder()
                .lastUpdated(mountTracker.getLastUpdated())
                .columns(columns)
                .players(minions)
                .build();

        return toResponse(response);
    }

    private int getLongestPlayerName(List<PlayerRS> players) {
        return players.stream()
                .map(PlayerRS::getName)
                .reduce((player1, player2) -> player1.length() > player2.length() ? player1 : player2)
                .orElse("")
                .length();
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
