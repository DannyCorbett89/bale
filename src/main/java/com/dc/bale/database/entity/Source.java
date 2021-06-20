package com.dc.bale.database.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Source {
    private String type;
    private String text;
    @JsonProperty("related_type")
    private String relatedType;
    @JsonProperty("related_id")
    private int relatedId;
}
