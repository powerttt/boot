package com.github.boot.commons.result;

public enum ResultConstants {
    /**
     * 异常定义
     */
    SERVER_NOT_FOUND(200, "test"),


    AUTH_INVALID(40101, "令牌失效或过期"),

    AUTH_NOT_LOGIN(40102, "请登入账号"),


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
