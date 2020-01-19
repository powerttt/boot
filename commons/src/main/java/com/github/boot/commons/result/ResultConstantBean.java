package com.github.boot.commons.result;

/**
 * 返回UpException异常异常时直接调用
 * @Author tongning
 * @Date 2019/10/22 0022
 *
 */
public class ResultConstantBean {
    private  int code;
    private  String msg;

    public ResultConstantBean(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
