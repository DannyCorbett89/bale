package com.dc.bale.controller;

import com.dc.bale.database.entity.FcRank;
import com.dc.bale.database.entity.Player;
import com.dc.bale.service.RankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.dc.bale.Constants.SUCCESS;

@Slf4j
@RequestMapping("/ranks")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RankController {
    private final RankService rankService;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<FcRank>> listRanks() {
        List<FcRank> fcRanks = rankService.listRanks();
        return ResponseEntity.ok(fcRanks);
    }

    @PostMapping(value = "/enable", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StatusResponse> enableRanks(@RequestBody List<FcRank> ranks, HttpServletRequest request) {
        rankService.setRanksEnabled(ranks);
        log.info("[{}] Updated rank visibility", request.getLocalAddr());
        return SUCCESS;
    }
}