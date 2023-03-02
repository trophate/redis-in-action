package com.trophate.redisinaction.enums;

public enum VoteType {
    SUPPORT(0),
    AGAINST(1);

    private final int code;

    VoteType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
