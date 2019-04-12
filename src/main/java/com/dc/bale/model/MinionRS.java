package com.dc.bale.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class MinionRS {
    private String id;
    private String name;
    private String url;
    @JsonIgnore
    private Map<String, String> players;

    @JsonAnyGetter
    public Map<String, String> getPlayers() {
        return players;
    }
}
