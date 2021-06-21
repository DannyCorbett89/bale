package com.dc.bale.model.ffxivcollect;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MinionsRS {
    private Map<String, String> query;
    private int count;
    private List<FFXIVCollectMinion> results;
}
