package com.dc.bale.controller;

import com.dc.bale.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/players")
@RestController
@RequiredArgsConstructor
public class PlayerController {
    private final PlayerService playerService;
}
