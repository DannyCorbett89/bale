package com.dc.bale.controller;

import com.dc.bale.database.entity.Mount;
import com.dc.bale.exception.MountException;
import com.dc.bale.model.AvailableMount;
import com.dc.bale.model.Response;
import com.dc.bale.service.PlayerTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;

import static com.dc.bale.Constants.SUCCESS;

@Slf4j
@RequestMapping("/mounts")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MountController {

    private final PlayerTracker playerTracker;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> listMounts() {
        Response response = Response.builder()
                .lastUpdated(playerTracker.getLastUpdated())
                .columns(playerTracker.getColumns(100, "Mount Name"))
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
}
