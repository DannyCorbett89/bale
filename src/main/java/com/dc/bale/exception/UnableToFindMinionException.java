package com.dc.bale.exception;

import com.dc.bale.model.MinionRS;

public class UnableToFindMinionException extends RuntimeException {
    public UnableToFindMinionException(MinionRS minion) {
        super(minion.toString());
    }
}
