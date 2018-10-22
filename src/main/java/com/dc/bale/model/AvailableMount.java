package com.dc.bale.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvailableMount {
    private long id;
    private String name;
}
