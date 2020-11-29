package com.dc.bale.exception;

public class JsonMappingException extends RuntimeException {
    public JsonMappingException(Exception e) {
        super(e);
    }
}
