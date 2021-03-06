package com.github.boot.gw.entity;


import java.util.List;

/**
 * @Author tongning
 * @Date 2019/7/28 0028
 * function:<
 * <p>
 * >
 */
public class Dubbo2MethodProperties {

    /**
     * 方法名
     */
    private String methodName;
    /**
     * RESTful
     */
    private String restPath;
    /**
     * request method
     */
    private String reqMethod;
    /**
     * 角色权限
     */
    private String roles;

    /**
     * 方法参数列表
     */
    private List<Dubbo2ParamProperties> paramList;

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getRestPath() {
        return restPath;
    }

    public void setRestPath(String restPath) {
        this.restPath = restPath;
    }

    public String getReqMethod() {
        return reqMethod;
    }

    public void setReqMethod(String reqMethod) {
        this.reqMethod = reqMethod;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<Dubbo2ParamProperties> getParamList() {
        return paramList;
    }

    public void setParamList(List<Dubbo2ParamProperties> paramList) {
        this.paramList = paramList;
    }
}
