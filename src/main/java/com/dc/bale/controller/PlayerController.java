package com.dc.bale.controller;

import com.dc.bale.component.JsonConverter;
import com.dc.bale.database.entity.FcRank;
import com.dc.bale.database.entity.Player;
import com.dc.bale.exception.PlayerException;
import com.dc.bale.service.PlayerService;
import com.dc.bale.service.RankService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/players")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlayerController {
    private final JsonConverter jsonConverter;
    private final PlayerService playerService;
    private final RankService rankService;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> listPlayers() {
        List<Long> rankIds = rankService.listEnabledRanks().stream()
                .map(FcRank::getId)
                .collect(Collectors.toList());
        List<Player> players = playerService.listPlayers(rankIds);
        return toResponse(players);
    }

    @PostMapping(value = "/add", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> addPlayer(@RequestParam("playerId") long playerId, HttpServletRequest request) {
        try {
            Player player = playerService.addPlayer(playerId);
            log.info("[{}] Added player: {}", request.getLocalAddr(), player.getName());
            return toResponse(StatusResponse.success());
        } catch (Exception e) {
            return toErrorResponse(e.getLocalizedMessage());
        }
    }

    // TODO: DeleteMapping
    @GetMapping(value = "/remove", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> removePlayer(@RequestParam(value = "playerName") String playerName, HttpServletRequest request) {
        try {
            playerService.removePlayer(playerName);
            log.info("[{}] Removed player: {}", request.getRemoteAddr(), playerName);
            return toResponse(StatusResponse.success());
        } catch (PlayerException e) {
            return toErrorResponse(e.getLocalizedMessage());
        }
    }

    private ResponseEntity<String> toErrorResponse(String message) {
        return toResponse(StatusResponse.error(message));
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
