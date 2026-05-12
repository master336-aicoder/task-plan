package com.taskplan.exception;

import com.taskplan.common.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ResultCode code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.ERROR;
    }

    public BusinessException(ResultCode code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode code) {
        super(code.getMsg());
        this.code = code;
    }
}
