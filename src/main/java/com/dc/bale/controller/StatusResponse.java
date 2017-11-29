package com.dc.bale.controller;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusResponse {
    private String status;
    private String message;

    public static StatusResponse success() {
        return StatusResponse.builder()
                .status("success")
                .build();
    }

    public static StatusResponse error(String message) {
        return StatusResponse.builder()
                .status("error")
                .message(message)
                .build();
    }
}
