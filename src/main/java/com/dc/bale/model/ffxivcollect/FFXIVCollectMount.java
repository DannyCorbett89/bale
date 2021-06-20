package com.dc.bale.model.ffxivcollect;

import com.dc.bale.database.entity.Source;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FFXIVCollectMount {
    private long id;
    private String name;
    private String description;
    @JsonProperty("enhanced_description")
    private String enhancedDescription;
    private String tooltip;
    private String movement;
    private int seats;
    private int order;
    @JsonProperty("order_group")
    private int orderGroup;
    private String patch;
    @JsonProperty("item_id")
    private int itemId;
    private String owned;
    private String image;
    private String icon;
    private List<Source> sources;
}
