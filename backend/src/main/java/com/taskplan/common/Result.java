package com.taskplan.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultCode.ERROR.getCode(), msg, null);
    }

    public static <T> Result<T> error(ResultCode code, String msg) {
        return new Result<>(code.getCode(), msg, null);
    }

    public static <T> Result<T> error(ResultCode code) {
        return new Result<>(code.getCode(), code.getMsg(), null);
    }
}
