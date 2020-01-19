package com.github.boot.gw.entity;

import io.jsonwebtoken.Claims;

import java.util.List;

/**
 * @Author tongning
 * @Date 2019/10/21 0021
 * function:<
 * <p>
 * >
 */
public class ServiceInfo {
    /**
     * 服务名（包名）
     */
    private String serverName;
    private String version;
    private String methodName;
    private List<String> paramTypes;
    private List<Object> params;
    private List<Dubbo2ParamProperties> paramProperties;

    /**
     * 认证信息
     */
    private String methodRoles;
    private Claims claims;

    public String getMethodRoles() {
        return methodRoles;
    }

    public void setMethodRoles(String methodRoles) {
        this.methodRoles = methodRoles;
    }

    public Claims getClaims() {
        return claims;
    }

    public void setClaims(Claims claims) {
        this.claims = claims;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<String> getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(List<String> paramTypes) {
        this.paramTypes = paramTypes;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public List<Dubbo2ParamProperties> getParamProperties() {
        return paramProperties;
    }

    public void setParamProperties(List<Dubbo2ParamProperties> paramProperties) {
        this.paramProperties = paramProperties;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
