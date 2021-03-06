package com.github.boot.gw.entity;

import java.util.Map;

/**
 * @Author tongning
 * @Date 2019/7/28 0028
 * function:<
 * <p>
 * >
 */
public class Dubbo2InterfaceProperties {
    /**
     * 服务名（包名）
     */
    private String serverName;

    /**
     * 服务方法 key-方法名称
     */
    private Map<String, Dubbo2MethodProperties> methods;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Map<String, Dubbo2MethodProperties> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, Dubbo2MethodProperties> methods) {
        this.methods = methods;
    }
}
