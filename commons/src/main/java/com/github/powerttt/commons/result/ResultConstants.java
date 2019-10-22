package com.github.powerttt.commons.result;

public enum ResultConstants {
    /**
     * 异常定义
     */
    SERVER_NOT_FOUND(200, "test"),




    ;

    private final int code;
    private final String msg;

    ResultConstants(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultConstantBean getResultConstants() {
        return new ResultConstantBean(code, msg);
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
