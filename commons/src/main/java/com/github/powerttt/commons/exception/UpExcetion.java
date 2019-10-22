package com.github.powerttt.commons.exception;

import lombok.Data;

/**
 * 自定义异常
 */
@Data
public class UpExcetion extends Exception {

    private int code;
    private String msg;

    public UpExcetion(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
