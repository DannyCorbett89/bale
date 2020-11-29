package com.dc.bale.controller;

import com.dc.bale.model.OAuthToken;
import com.dc.bale.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/oauth")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OAuthController {
    private final OAuthService oAuthService;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<OAuthToken> getOAuthToken() {
        OAuthToken token = oAuthService.getOAuthToken();

        return ResponseEntity.ok(token);
    }
}
