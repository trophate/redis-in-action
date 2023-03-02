package com.trophate.redisinaction.enums;

public enum ResultCode {
    SUCCESS(1, "success"),
    FAIL(-1, "fail");

    private final int code;
    private final String value;

    ResultCode(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
