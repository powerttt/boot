package com.github.powerttt.commons.result;

import java.io.Serializable;

public class ResultBean<T> implements Serializable {

    private int code;
    private Object msg;
    private T data;

    public ResultBean() {
    }

    public ResultBean(int code, Object msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResultBean success(T data) {
        return this.success(data, "success");
    }

    public ResultBean success(T data, String msg) {
        this.data = data;
        this.msg = msg;
        this.code = 200;
        return this;
    }

    public ResultBean<T> failure(Integer code, String msg) {
        this.code = code;
        this.data = null;
        this.msg = msg;
        return this;
    }

    public ResultBean<T> failure(String code, String msg) {
        this.code = Integer.valueOf(code);
        this.data = null;
        this.msg = msg;
        return this;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getMsg() {
        return msg;
    }

    public void setMsg(Object msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
