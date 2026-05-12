package com.taskplan.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200000, "成功"),
    ERROR(500000, "系统错误"),
    PARAM_ERROR(400000, "参数错误"),
    NOT_FOUND(404000, "资源不存在"),
    UNAUTHORIZED(401000, "未授权"),
    FORBIDDEN(403000, "禁止访问");

    private final Integer code;
    private final String msg;
}
