package com.dc.bale.controller;

import com.dc.bale.database.entity.Mount;
import com.dc.bale.database.entity.Player;
import com.dc.bale.exception.MountException;
import com.dc.bale.exception.PlayerException;
import com.dc.bale.model.AvailableMount;
import com.dc.bale.model.Response;
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
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static com.dc.bale.Constants.SUCCESS;

@Slf4j
@RequestMapping("/mounts")
@RestController
@RequiredArgsConstructor
public class MountController {
    private final PlayerTracker playerTracker;
    private final PlayerService playerService;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> listMounts() {
        Response response = Response.builder()
                .lastUpdated(playerTracker.getLastUpdated())
                .columns(playerTracker.getMountColumns(200))
                .players(playerTracker.getMounts())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/available", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<AvailableMount>> listAvailableMounts() {
        return ResponseEntity.ok(playerTracker.getAvailableMounts());
    }

    @PostMapping(value = "/add", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StatusResponse> addMount(@RequestParam("name") String name, HttpServletRequest request) {
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(StatusResponse.error("Missing required parameter: name"));
        }

        log.info("[{}] Added mount: {}", request.getRemoteAddr(), name);

        try {
            playerTracker.addMount(name);
            return SUCCESS;
        } catch (MountException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StatusResponse.error(e.getMessage()));
        }
    }

    @Transactional
    @DeleteMapping(value = "/remove", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StatusResponse> removeMount(@RequestParam("id") long id, HttpServletRequest request) {
        if (id == 0) {
            return ResponseEntity.badRequest()
                    .body(StatusResponse.error("Missing required parameter: id"));
        }

        try {
            Mount mount = playerTracker.removeMount(id);
            log.info("[{}] Removed mount: {}", request.getRemoteAddr(), mount.getName());
            return SUCCESS;
        } catch (MountException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StatusResponse.error(e.getMessage()));
        }
    }

    @GetMapping(value = "/players", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Player>> listPlayers() {
        List<Player> players = playerService.listPlayersForMounts();
        return ResponseEntity.ok(players);
    }

    @GetMapping(value = "/players/visible", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Player>> listVisiblePlayers() {
        List<Player> players = playerService.listVisiblePlayersForMounts();
        return ResponseEntity.ok(players);
    }

    @PostMapping(value = "/players/visible", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StatusResponse> setVisiblePlayers(@RequestBody List<Player> players) {
        playerService.setPlayersVisibleForMounts(players);
        return SUCCESS;
    }

    @PostMapping(value = "/players/add", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StatusResponse> addPlayer(@RequestParam("ids") List<Long> ids, HttpServletRequest request) {
        try {
            List<Player> players = playerService.addPlayersForMounts(ids);
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

            Player player = playerService.removePlayerForMounts(id, name);
            log.info("[{}] Removed player: {}", request.getRemoteAddr(), player.getName());
            return SUCCESS;
        } catch (PlayerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StatusResponse.error(e.getLocalizedMessage()));
        }
    }
}
