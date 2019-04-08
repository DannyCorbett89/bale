package com.dc.bale.controller;

import com.dc.bale.component.JsonConverter;
import com.dc.bale.database.Mount;
import com.dc.bale.exception.MountException;
import com.dc.bale.model.Response;
import com.dc.bale.service.MountTracker;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

@Slf4j
@RequestMapping("/mounts")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MountController {
    private final JsonConverter jsonConverter;
    private final MountTracker mountTracker;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
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

    @GetMapping(value = "/available", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> listAvailableMounts() {
        return toResponse(mountTracker.getAvailableMounts());
    }

    @PostMapping(value = "/add", produces = {MediaType.APPLICATION_JSON_VALUE})
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

    // TODO: DeleteMapping
    @Transactional
    @GetMapping(value = "/remove", produces = {MediaType.APPLICATION_JSON_VALUE})
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
