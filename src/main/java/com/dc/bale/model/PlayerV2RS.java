package com.dc.bale.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerV2RS {
    private String name;
    private boolean owned;
}
