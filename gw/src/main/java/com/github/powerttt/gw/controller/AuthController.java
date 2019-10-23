package com.github.powerttt.gw.controller;

import com.github.powerttt.commons.result.ResultBean;
import com.github.powerttt.gw.jwt.JWTUtils;
import com.github.powerttt.gw.utils.GwConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证controller
 */
@Slf4j
@RestController()
@RequestMapping("auth")
public class AuthController {


    @Autowired
    private JWTUtils jwtUtils;

    @PostMapping("login")
    public ResultBean login(String username, String password) {
        log.info("{}  &  {}", username, password);
        Map<String, String> map = new HashMap<>();
        map.put(GwConstants.USERNAME, "tn");
        map.put(GwConstants.JWT_ROLES, "ADMIN,TEST,TEACHER");
        String token = jwtUtils.createJWT("1", "{username:u}", map);
        log.info("token : {}", token);
        return new ResultBean().success(token);
    }

    @PostMapping("refresh")
    public ResultBean refresh(HttpServletRequest request) {
        String token = jwtUtils.refresh(request);
        log.info("refresh token : {}", token);
        return new ResultBean().success(token);
    }

    @GetMapping("test")
    public ResultBean test() {
        Map<String, String> map = new HashMap<>();
        map.put(GwConstants.USERNAME, "test");
        map.put(GwConstants.JWT_ROLES, "TEST");
        String token = jwtUtils.createJWT("1", "{username:u}", map);
        log.info("token : {}", token);
        return new ResultBean().success(token);
    }


}
