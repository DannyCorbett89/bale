package com.dc.bale.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SourceType {
    TRIALS(16),
    RAIDS(14);

    private int typeId;
}
