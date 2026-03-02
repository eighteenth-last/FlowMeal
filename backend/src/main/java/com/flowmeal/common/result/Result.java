package com.flowmeal.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一 API 返回结构
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;

    private Result() {}

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.message = ResultCode.SUCCESS.getMessage();
        result.data = data;
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.message = message;
        result.data = data;
        return result;
    }

    public static <T> Result<T> fail(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.code = resultCode.getCode();
        result.message = resultCode.getMessage();
        return result;
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }

    public static <T> Result<T> fail(String message) {
        return fail(ResultCode.FAIL.getCode(), message);
    }
}
