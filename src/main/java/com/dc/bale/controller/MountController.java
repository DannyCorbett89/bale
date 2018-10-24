package com.dc.bale.controller;

import com.dc.bale.component.JsonConverter;
import com.dc.bale.exception.MountException;
import com.dc.bale.exception.PlayerException;
import com.dc.bale.model.Response;
import com.dc.bale.service.MountTracker;
import com.dc.bale.service.PlayerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.io.IOException;

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

    @RequestMapping(value = "/players", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> listPlayers() {
        return toResponse(playerService.listPlayers());
    }

    @RequestMapping(value = "/listMounts", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> listMounts(@RequestParam(value = "refresh", required = false) String refresh) throws IOException {
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
    public ResponseEntity<String> addPlayer(@RequestParam("playerId") long playerId) {
        try {
            playerService.addPlayer(playerId);
            return toResponse(StatusResponse.success());
        } catch (Exception e) {
            return toErrorResponse(e.getLocalizedMessage());
        }
    }

    @RequestMapping(value = "/removePlayer", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> removePlayer(@RequestParam(value = "playerName") String playerName) {
        try {
            playerService.removePlayer(playerName);
            return toResponse(StatusResponse.success());
        } catch (PlayerException e) {
            return toErrorResponse(e.getLocalizedMessage());
        }
    }

    @RequestMapping(value = "/addMount", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> addMount(@RequestParam("name") String name) throws IOException {
        if (name == null || name.isEmpty()) {
            return toErrorResponse("Missing required parameter: name");
        }

        try {
            mountTracker.addMount(name);
            return toResponse(StatusResponse.success());
        } catch (MountException e) {
            return toErrorResponse(e.getMessage());
        }
    }

    @Transactional
    @RequestMapping(value = "/removeMount", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> removeMount(@RequestParam("id") long id) throws IOException {
        if (id == 0) {
            return toErrorResponse("Missing required parameter: id");
        }

        try {
            mountTracker.removeMount(id);
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
