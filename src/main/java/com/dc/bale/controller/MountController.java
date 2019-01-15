package com.dc.bale.controller;

import com.dc.bale.component.JsonConverter;
import com.dc.bale.database.FcRank;
import com.dc.bale.database.Mount;
import com.dc.bale.database.Player;
import com.dc.bale.exception.MountException;
import com.dc.bale.exception.PlayerException;
import com.dc.bale.model.Response;
import com.dc.bale.service.MountTracker;
import com.dc.bale.service.PlayerService;
import com.dc.bale.service.RankService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MountController {
    @NonNull
    private JsonConverter jsonConverter;
    @NonNull
    private MountTracker mountTracker;
    @NonNull
    private PlayerService playerService;
    @NonNull
    private RankService rankService;

    @RequestMapping(value = "/players", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> listPlayers() {
        List<Long> rankIds = rankService.listEnabledRanks().stream()
                .map(FcRank::getId)
                .collect(Collectors.toList());
        List<Player> players = playerService.listPlayers(rankIds);
        return toResponse(players);
    }

    @RequestMapping(value = "/ranks", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> listRanks() {
        List<FcRank> fcRanks = rankService.listRanks();
        return toResponse(fcRanks);
    }

    @RequestMapping(value = "/ranks/enable", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> enableRanks(@RequestBody String ranksString) throws IOException {
        List<FcRank> ranks = jsonConverter.toList(ranksString, FcRank.class);
        rankService.setRanksEnabled(ranks);
        Map<String, String> responseMap = Collections.singletonMap("success", "true");
        return toResponse(responseMap);
    }

    @RequestMapping(value = "/listMounts", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> listMounts(@RequestParam(value = "refresh", required = false) String refresh) {
        if (refresh != null && refresh.equals("true")) {
            mountTracker.loadMounts();
        }

        Response response = Response.builder()
                .lastUpdated(mountTracker.getLastUpdated())
                .players(mountTracker.getMounts())
                .build();

        return toResponse(response);
    }

    @RequestMapping(value = "/listAvailableMounts", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> listAvailableMounts() {
        return toResponse(mountTracker.getAvailableMounts());
    }

    @RequestMapping(value = "/addPlayer", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> addPlayer(@RequestParam("playerId") long playerId, HttpServletRequest request) {
        try {
            Player player = playerService.addPlayer(playerId);
            log.info("[{}] Added player: {}", request.getLocalAddr(), player.getName());
            return toResponse(StatusResponse.success());
        } catch (Exception e) {
            return toErrorResponse(e.getLocalizedMessage());
        }
    }

    @RequestMapping(value = "/removePlayer", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> removePlayer(@RequestParam(value = "playerName") String playerName, HttpServletRequest request) {
        try {
            playerService.removePlayer(playerName);
            log.info("[{}] Removed player: {}", request.getRemoteAddr(), playerName);
            return toResponse(StatusResponse.success());
        } catch (PlayerException e) {
            return toErrorResponse(e.getLocalizedMessage());
        }
    }

    @RequestMapping(value = "/addMount", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> addMount(@RequestParam("name") String name, HttpServletRequest request) {
        if (name == null || name.isEmpty()) {
            return toErrorResponse("Missing required parameter: name");
        }

        log.info("[{}] Added mount: {}", request.getRemoteAddr(), name);

        try {
            mountTracker.addMount(name);
            return toResponse(StatusResponse.success());
        } catch (MountException e) {
            return toErrorResponse(e.getMessage());
        }
    }

    @Transactional
    @RequestMapping(value = "/removeMount", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> removeMount(@RequestParam("id") long id, HttpServletRequest request) {
        if (id == 0) {
            return toErrorResponse("Missing required parameter: id");
        }

        try {
            Mount mount = mountTracker.removeMount(id);
            log.info("[{}] Removed mount: {}", request.getRemoteAddr(), mount.getName());
            return toResponse(StatusResponse.success());
        } catch (MountException e) {
            return toErrorResponse(e.getMessage());
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
