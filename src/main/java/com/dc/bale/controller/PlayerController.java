package com.dc.bale.controller;

import com.dc.bale.database.dao.PlayerRepository;
import com.dc.bale.database.entity.FcRank;
import com.dc.bale.database.entity.Player;
import com.dc.bale.exception.PlayerException;
import com.dc.bale.service.PlayerService;
import com.dc.bale.service.RankService;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.dc.bale.Constants.SUCCESS;

@Slf4j
@RequestMapping("/players")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlayerController {
    private final PlayerService playerService;
    private final PlayerRepository playerRepository;
    private final RankService rankService;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Player>> listPlayers() {
        List<Long> rankIds = rankService.listEnabledRanks().stream()
                .map(FcRank::getId)
                .collect(Collectors.toList());
        List<Player> players = playerService.listPlayers(rankIds);
        return ResponseEntity.ok(players);
    }

    @GetMapping(value = "/visible", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Player>> listVisiblePlayers() {
        List<Player> players = playerRepository.findByVisibleTrue().stream()
                .sorted(Comparator.comparing(Player::getName))
                .collect(Collectors.toList());
        return ResponseEntity.ok(players);
    }

    @PostMapping(value = "/visible", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StatusResponse> setVisiblePlayers(@RequestBody List<Player> players) {
        playerService.setPlayersVisible(players);
        return SUCCESS;
    }

    @PostMapping(value = "/add", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StatusResponse> addPlayer(@RequestParam("playerId") long playerId, HttpServletRequest request) {
        try {
            Player player = playerService.addPlayer(playerId);
            log.info("[{}] Added player: {}", request.getLocalAddr(), player.getName());
            return SUCCESS;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StatusResponse.error(e.getLocalizedMessage()));
        }
    }

    @DeleteMapping(value = "/remove", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StatusResponse> removePlayer(@RequestParam(value = "id", required = false) Long id,
                                                       @RequestParam(value = "name", required = false) String name,
                                                       HttpServletRequest request) {
        try {
            if (Strings.isNullOrEmpty(name) && id == null) {
                return ResponseEntity.badRequest()
                        .body(StatusResponse.error("Player name or id must be provided"));
            }

            Player player = playerService.removePlayer(id, name);
            log.info("[{}] Removed player: {}", request.getRemoteAddr(), player.getName());
            return SUCCESS;
        } catch (PlayerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StatusResponse.error(e.getLocalizedMessage()));
        }
    }
}
