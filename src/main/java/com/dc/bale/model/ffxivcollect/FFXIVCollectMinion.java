package com.dc.bale.model.ffxivcollect;

import com.dc.bale.database.entity.Source;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FFXIVCollectMinion {
    private long id;
    private String name;
    private String description;
    @JsonProperty("enhanced_description")
    private String enhancedDescription;
    private String tooltip;
    private String patch;
    @JsonProperty("item_id")
    private int itemId;
    private Map<String, Object> behavior;
    private Map<String, Object> race;
    private String owned;
    private String image;
    private String icon;
    private List<Source> sources;
}