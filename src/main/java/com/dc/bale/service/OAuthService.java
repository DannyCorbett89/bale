package com.dc.bale.service;

import com.dc.bale.component.HttpClient;
import com.dc.bale.model.OAuthToken;
import com.dc.bale.model.TokenRS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {
    private static final String TOKEN_URL = "https://www.fflogs.com/oauth/token";

    private final ConfigService configService;
    private final HttpClient httpClient;

    private Instant expiryDate = Instant.now();
    private OAuthToken token;

    public OAuthToken getOAuthToken() {
        Instant currentTime = Instant.now();
        if (currentTime.isAfter(expiryDate)) {
            String clientID = configService.getMandatoryConfig("clientID");
            String clientSecret = configService.getMandatoryConfig("clientSecret");
            String formData = "name=\"grant_type\"\r\n\r\nclient_credentials";

            TokenRS response = httpClient.multipart(TOKEN_URL, clientID, clientSecret, formData, TokenRS.class);

            expiryDate = currentTime.plus(response.getExpiresIn(), ChronoUnit.SECONDS);
            token = new OAuthToken(response.getAccessToken());
        }

        return token;
    }
}
