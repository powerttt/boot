package com.github.powerttt.gw.jwt;

import com.alibaba.fastjson.JSON;
import com.github.powerttt.commons.exception.UpExcetion;
import com.github.powerttt.commons.result.ResultBean;
import com.github.powerttt.commons.result.ResultConstantBean;
import com.github.powerttt.commons.result.ResultConstants;
import com.github.powerttt.gw.entity.ServiceInfo;
import com.github.powerttt.gw.utils.GwConstants;
import com.github.powerttt.gw.utils.GwRequestUriUtil;
import com.github.powerttt.gw.utils.GwResponseUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    private GwRequestUriUtil gwRequestUriUtil;

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
        String header = request.getHeader(GwConstants.AUTHORIZATION);
        if (StringUtils.isNotEmpty(header) && header.startsWith(GwConstants.BEARER)) {
            String token = header.substring(GwConstants.BEARER.length());
            try {
                // 解析
                Claims claims = jwtUtils.parseJWT(token);
                if (claims != null) {
                    ServiceInfo serviceInfo = gwRequestUriUtil.generateInterfaceInfo(request, response);
                    boolean roleFlag = verifyRoles(serviceInfo, claims.get(GwConstants.JWT_ROLES));
                    // 没有权限
                    if (!roleFlag) {
                        log.warn("user: {} 401 UNAUTHORIZED by URI: {}", claims.get(GwConstants.USERNAME), request.getRequestURI());
                        GwResponseUtils.writeResponse(response, HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.name());
                        return false;
                    }
                    request.setAttribute(ServiceInfo.class.getSimpleName(), serviceInfo);
                    return true;
                }
            } catch (UpExcetion upExcetion) {
                GwResponseUtils.writeResponse(response, upExcetion.getCode(), upExcetion.getMsg());
            } catch (Exception e) {
                log.warn("JWT解析有误");
                if (response.getWriter() == null) {
                    GwResponseUtils.writeResponse(response, ResultConstants.AUTH_INVALID.getResultConstants());
                }
            }
        } else {
            log.warn("用户未登入");
            GwResponseUtils.writeResponse(response, ResultConstants.AUTH_NOT_LOGIN.getResultConstants());
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
                String[] userRoleArr = userRoleStr.split(",");
                // 校验用户权限
                for (String s : userRoleArr) {
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
