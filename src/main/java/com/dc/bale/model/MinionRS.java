package com.dc.bale.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MinionRS {
    private long id;
    private String name;
    private String url;
}
