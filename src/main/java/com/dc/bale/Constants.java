package com.dc.bale;

import com.dc.bale.controller.StatusResponse;
import org.springframework.http.ResponseEntity;

public class Constants {
    public static final String BASE_URL = "https://na.finalfantasyxiv.com";
    private static final String LODESTONE_URL = BASE_URL + "/lodestone";
    private static final String DB_URL = LODESTONE_URL + "/playguide/db";
    public static final String DUTY_URL = DB_URL + "/duty";
    public static final String TRIALS_URL = DUTY_URL + "/?category2=4";
    private static final String ITEM_URL = DB_URL + "/item";
    public static final String MINIONS_URL = ITEM_URL + "/?category2=7&category3=81";

    public static final ResponseEntity<StatusResponse> SUCCESS = ResponseEntity.ok(StatusResponse.success());
}
