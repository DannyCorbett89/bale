package com.dc.bale.controller;

import com.dc.bale.component.JsonConverter;
import com.dc.bale.database.FcRank;
import com.dc.bale.service.RankService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/ranks")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RankController {
    private final JsonConverter jsonConverter;
    private final RankService rankService;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> listRanks() {
        List<FcRank> fcRanks = rankService.listRanks();
        return toResponse(fcRanks);
    }

    @PostMapping(value = "/enable", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> enableRanks(@RequestBody String ranksString) throws IOException {
        List<FcRank> ranks = jsonConverter.toList(ranksString, FcRank.class);
        rankService.setRanksEnabled(ranks);
        Map<String, String> responseMap = Collections.singletonMap("success", "true");
        return toResponse(responseMap);
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