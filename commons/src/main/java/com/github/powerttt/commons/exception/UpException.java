package com.github.powerttt.commons.exception;

import com.github.powerttt.commons.result.ResultConstantBean;
import lombok.Data;

/**
 * 自定义异常
 */
@Data
public class UpException extends RuntimeException {

    private int code;
    private String msg;

    public UpException() {
    }

    public UpException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }


    /**
     * 返回枚举定义的异常
     *
     * @param resultConstantBean
     */
    public UpException(ResultConstantBean resultConstantBean) {
        super(resultConstantBean.getMsg());
        this.code = resultConstantBean.getCode();
        this.msg = resultConstantBean.getMsg();
    }

    @Override
    public String toString() {
        return "{\"code\":" + code + ",\"msg\":\"" + msg + "\"}";
    }
}
