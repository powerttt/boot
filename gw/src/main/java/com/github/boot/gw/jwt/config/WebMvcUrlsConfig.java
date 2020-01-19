package com.github.boot.gw.jwt.config;

import com.changyuan.education.gw.jwt.JWTFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Component
public class WebMvcUrlsConfig extends WebMvcConfigurationSupport {
    @Autowired
    private JWTFilter jwtFilter;
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtFilter).addPathPatterns("/**");
    }
}
