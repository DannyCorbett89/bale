package com.dc.bale.controller;

import com.dc.bale.model.MinionsResponse;
import com.dc.bale.service.MinionService;
import com.dc.bale.service.PlayerTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/minions")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MinionController {
    private final PlayerTracker playerTracker;
    private final MinionService minionService;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<MinionsResponse> listMinions() {
        MinionsResponse response = MinionsResponse.builder()
                .lastUpdated(playerTracker.getLastUpdated())
                .columns(playerTracker.getColumns(200))
                .players(minionService.getMinions())
                .build();

        return ResponseEntity.ok(response);
    }
}
