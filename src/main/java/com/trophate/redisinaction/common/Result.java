package com.trophate.redisinaction.common;

import com.trophate.redisinaction.enums.ResultCode;

public class Result {

    private int code;
    private String message;
    private Object data;

    public int getCode() {
        return code;
    }

    public Result setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Result setMessage(String message) {
        this.message = message;
        return this;
    }

    public Object getData() {
        return data;
    }

    public <T> Result setData(T data) {
        this.data = data;
        return this;
    }

    public static Result success() {
        return new Result().setCode(ResultCode.SUCCESS.getCode()).setMessage(ResultCode.SUCCESS.getValue());
    }

    public static Result fail() {
        return new Result().setCode(ResultCode.FAIL.getCode()).setMessage(ResultCode.FAIL.getValue());
    }
}
