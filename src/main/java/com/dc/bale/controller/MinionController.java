package com.dc.bale.controller;

import com.dc.bale.database.entity.Player;
import com.dc.bale.exception.PlayerException;
import com.dc.bale.model.MinionsResponse;
import com.dc.bale.service.MinionService;
import com.dc.bale.service.PlayerService;
import com.dc.bale.service.PlayerTracker;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.dc.bale.Constants.SUCCESS;

@Slf4j
@RequestMapping("/minions")
@RestController
@RequiredArgsConstructor
public class MinionController {
    private final PlayerTracker playerTracker;
    private final PlayerService playerService;
    private final MinionService minionService;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<MinionsResponse> listMinions() {
        MinionsResponse response = MinionsResponse.builder()
                .lastUpdated(playerTracker.getLastUpdated())
                .columns(playerTracker.getMinionColumns(200))
                .players(minionService.getMinions())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/players", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Player>> listPlayers() {
        List<Player> players = playerService.listPlayersForMinions();
        return ResponseEntity.ok(players);
    }

    @GetMapping(value = "/players/visible", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Player>> listVisiblePlayers() {
        List<Player> players = playerService.listVisiblePlayersForMinions();
        return ResponseEntity.ok(players);
    }

    @PostMapping(value = "/players/visible", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StatusResponse> setVisiblePlayers(@RequestBody List<Player> players) {
        playerService.setPlayersVisibleForMinions(players);
        return SUCCESS;
    }

    @PostMapping(value = "/players/add", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StatusResponse> addPlayer(@RequestParam("ids") List<Long> ids, HttpServletRequest request) {
        try {
            List<Player> players = playerService.addPlayersForMinions(ids);
            log.info("[{}] Added players: {}", request.getLocalAddr(), players.stream().map(Player::getName).collect(Collectors.toList()));
            return SUCCESS;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StatusResponse.error(e.getLocalizedMessage()));
        }
    }

    @DeleteMapping(value = "/players/remove", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StatusResponse> removePlayer(@RequestParam(value = "id", required = false) Long id,
                                                       @RequestParam(value = "name", required = false) String name,
                                                       HttpServletRequest request) {
        try {
            if (Strings.isNullOrEmpty(name) && id == null) {
                return ResponseEntity.badRequest()
                        .body(StatusResponse.error("Player name or id must be provided"));
            }

            Player player = playerService.removePlayerForMinions(id, name);
            log.info("[{}] Removed player: {}", request.getRemoteAddr(), player.getName());
            return SUCCESS;
        } catch (PlayerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StatusResponse.error(e.getLocalizedMessage()));
        }
    }
}
