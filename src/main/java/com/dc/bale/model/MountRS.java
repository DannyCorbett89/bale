package com.dc.bale.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class MountRS {
    private long id;
    private String name;
    private String instance;
    @JsonIgnore
    private Map<String, String> players;

    @JsonAnyGetter
    public Map<String, String> getPlayers() {
        return players;
    }
}
