package com.dc.bale.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Column {
    private String key;
    private String name;
    private int width;
    private boolean frozen;
}
