package com.grady.bubbo.common.exceptions;

public class BubboException extends RuntimeException {

    private String code;

    public BubboException(String code, String msg) {
        super(msg);
        this.code = code;
    }
}
