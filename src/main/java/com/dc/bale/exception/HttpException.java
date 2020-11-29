package com.dc.bale.exception;

public class HttpException extends RuntimeException {
    public HttpException(Exception e) {
        super(e);
    }
}
