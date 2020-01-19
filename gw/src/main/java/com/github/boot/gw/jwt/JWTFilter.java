package com.github.boot.gw.jwt;

import com.alibaba.fastjson.JSON;
import com.github.boot.commons.JWTUtils;
import com.github.boot.gw.entity.ServiceInfo;
import com.github.boot.gw.utils.GwResponseUtils;
import com.github.boot.commons.constants.JwtKeys;
import com.github.boot.commons.exception.UpException;
import com.github.boot.commons.result.ResultConstants;
import com.github.boot.gw.controller.GwParseRequestURI;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("gw.jwt-judge")
    private Boolean jwtJudge;

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
        // jwtJudge == false  pass
        if (!jwtJudge) {
            return true;
        }
        //只是解析token
        String header = request.getHeader(JwtKeys.AUTHORIZATION);
        if (StringUtils.isNotEmpty(header) && header.startsWith(JwtKeys.BEARER)) {
            String token = header.substring(JwtKeys.BEARER.length());
            Claims claims = null;
            try {
                // 解析
                claims = jwtUtils.parseJWT(token);
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
