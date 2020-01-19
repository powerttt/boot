package com.github.powerttt.gw.jwt;

import com.alibaba.fastjson.JSON;
import com.github.boot.commons.JWTUtils;
import com.github.powerttt.commons.constants.JwtKeys;
import com.github.powerttt.commons.exception.UpException;
import com.github.powerttt.commons.result.ResultConstants;
import com.github.powerttt.gw.controller.GwParseRequestURI;
import com.github.powerttt.gw.entity.ServiceInfo;
import com.github.powerttt.gw.utils.GwResponseUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

/**
 * @Author tongning
 * @Date 2019/10/20 0020
 * function:<
 * <p> JWT 拦截
 * >
 */
@Slf4j
@Component
public class JWTFilter implements HandlerInterceptor {

    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private GwParseRequestURI gwParseRequestURI;

    /**
     * 请求之前处理
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //只是解析token
        String header = request.getHeader(JwtKeys.AUTHORIZATION);
        String up_data = request.getRequestURI().substring(1).split("/")[0];
        if ("up_data".equals(up_data)) {
            if ("POST".equals(request.getMethod())) {
                StringBuffer data = new StringBuffer();
                String line = null;
                BufferedReader reader = null;
                reader = request.getReader();
                while (null != (line = reader.readLine())) {
                    data.append(line);
                }
                String bodyParams = URLDecoder.decode(data.toString(), "UTF-8");
                log.info("body : {}", bodyParams);
                log.info("HTTP POST Params: {}", bodyParams);
            } else {
                log.info("HTTP GET Params: {}", JSON.toJSONString(request.getParameterMap()));
            }

        }
        if (StringUtils.isNotEmpty(header) && header.startsWith(JwtKeys.BEARER)) {
            String token = header.substring(JwtKeys.BEARER.length());
            Claims claims = null;
            try {
                // 解析
//                claims = jwtUtils.parseJWT(token);
            } catch (Exception e) {
                log.warn("JWT解析有误");
                GwResponseUtils.writeResponse(response, ResultConstants.AUTH_INVALID.getResultConstants());
            }
            try {
                if (claims != null) {
                    // 文件格式请求
                    if (ServletFileUpload.isMultipartContent(request)) {
                        request.setAttribute(Claims.class.getSimpleName(), claims);
                        return true;
                    }
                    ServiceInfo serviceInfo = gwParseRequestURI.generateInterfaceInfo(request, response);
                    serviceInfo.setClaims(claims);
                    request.setAttribute(ServiceInfo.class.getSimpleName(), serviceInfo);
                    return true;
                }
            } catch (UpException upException) {
                GwResponseUtils.writeResponse(response, upException.getCode(), upException.getMsg());
                upException.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            log.warn("用户未登入");
            GwResponseUtils.writeResponse(response, ResultConstants.AUTH_NOT_LOGIN.getResultConstants());
            return false;
        }
        return false;
    }

    /**
     * 校验是否包含权限
     *
     * @param serviceInfo
     * @param userRoles
     * @return true 通行  false 权限不够
     */
    private boolean verifyRoles(ServiceInfo serviceInfo, Object userRoles) {

        // 接口配置了权限
        if (serviceInfo.getMethodRoles() != null) {
            // 获取方法权限
            String methodRoleStr = serviceInfo.getMethodRoles();
            if ("ALL".equals(methodRoleStr)) {
                return true;
            }
            // 获取当前用户权限
            String userRoleStr = (String) userRoles;
            // 用户配置权限
            if (userRoleStr != null) {
                List<String> userRoleList = JSON.parseArray(userRoleStr, String.class);
                // 校验用户权限
                for (String s : userRoleList) {
                    // 包含权限
                    if (methodRoleStr.contains(s)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
