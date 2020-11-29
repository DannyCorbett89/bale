package com.dc.bale.model;

import lombok.Data;

@Data
public class OAuthToken {
    private final String token;

    public OAuthToken(String token) {
        this.token = token;
    }
}
