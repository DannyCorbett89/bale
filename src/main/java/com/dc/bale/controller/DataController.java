package com.dc.bale.controller;

import com.dc.bale.service.PlayerTracker;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RequestMapping("/data")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataController {
    private final PlayerTracker playerTracker;

    @GetMapping(value = "refresh", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, String>> refresh() {
        playerTracker.loadMounts();

        String lastUpdated = playerTracker.getLastUpdated();
        Map<String, String> response = ImmutableMap.of("lastUpdated", lastUpdated);

        return ResponseEntity.ok()
                .body(response);
    }
}
