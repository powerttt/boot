package com.github.boot.gw.utils;

import com.alibaba.fastjson.JSON;
import com.github.boot.commons.result.ResultConstantBean;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 响应工具类
 */
public class GwResponseUtils {


    public static void writeResponse(HttpServletResponse response, int code, String msg) throws IOException {
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE);
        response.getWriter().write(JSON.toJSONString(new ResultConstantBean(code, msg)));
    }

    public static void writeResponse(HttpServletResponse response, ResultConstantBean resultConstantBean) throws IOException {
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE);
        response.getWriter().write(JSON.toJSONString(resultConstantBean));
    }
}
