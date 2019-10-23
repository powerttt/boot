package com.github.powerttt.gw.utils;

import com.github.powerttt.commons.exception.UpExcetion;
import com.github.powerttt.gw.config.Dubbo2RouterProperties;
import com.github.powerttt.gw.entity.Dubbo2InterfaceProperties;
import com.github.powerttt.gw.entity.Dubbo2MethodProperties;
import com.github.powerttt.gw.entity.Dubbo2ParamProperties;
import com.github.powerttt.gw.entity.ServiceInfo;
import com.github.powerttt.gw.enums.Dubbo2ParamTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析请求服务方法工具类
 */
@Slf4j
@Component
public class GwRequestUriUtil {

    @Autowired
    private Dubbo2RouterProperties dubbo2RouterProperties;

    /**
     * 获取请求中的参数
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @return
     * @throws UpExcetion
     */
    public ServiceInfo generateInterfaceInfo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws UpExcetion {
        String uri = httpServletRequest.getRequestURI();
        log.info("generateInterfaceInfo URI : {}", uri);
        String[] uris = uri.substring(1).split("/");
        // 请求格式有误
        if (uris.length < 2) {
            log.warn("generateInterfaceInfo URI ：{}  解析错误", uri);
            throw new UpExcetion(40401, "URI解析错误");
        }
        String serviceName = uris[0];
        Dubbo2InterfaceProperties dubbo2InterfaceProperties = dubbo2RouterProperties.getRouters().get(serviceName);
        if (dubbo2InterfaceProperties == null) {
            log.warn("generateInterfaceInfo interfaceName ：{}  未找到此服务配置", serviceName);
            throw new UpExcetion(40402, "未找到此服务配置");
        }
        String methodName = uris[1];
        if (StringUtils.isEmpty(methodName)) {
            log.warn("generateInterfaceInfo method ：{}  解析错误", uri);
        }
        assert dubbo2InterfaceProperties != null;

        Dubbo2MethodProperties dubbo2MethodProperties = dubbo2InterfaceProperties.getMethods().get(methodName);
        if (dubbo2MethodProperties == null) {
            if (dubbo2InterfaceProperties == null) {
                log.warn("generateInterfaceInfo method ：{}  未找到此服务方法", methodName);
                throw new UpExcetion(40402, "未找到此服务配置");
            }
        }
        if (null == dubbo2MethodProperties) {
            log.warn("generateInterfaceInfo method ：{}  请配置请求方法", methodName);
            throw new UpExcetion(40402, "未找到配置的服务方法");
        }
        // 校验请求是否对应
        if (!httpServletRequest.getMethod().equals(dubbo2MethodProperties.getReqMethod())) {
            log.warn("generateInterfaceInfo Http method ：{}  请求动作错误， conf: {}", httpServletRequest.getMethod(), dubbo2MethodProperties.getReqMethod());
            throw new UpExcetion(40402, "未找到配置的服务方法");
        }
        // 获取方法info
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setServerName(dubbo2InterfaceProperties.getServerName());
        serviceInfo.setMethodName(methodName);
        // 方法无权限
        if (dubbo2MethodProperties.getRoles() != null) {
            serviceInfo.setMethodRoles(dubbo2MethodProperties.getRoles());
        } else {
            log.debug("方法 {} 未设定角色", methodName);
        }


        List<String> paramTypeList = new ArrayList<>();
        List<Object> paramList = new ArrayList<>();

        if (dubbo2MethodProperties.getParamList() == null) {
            log.warn("generateInterfaceInfo method ：{}  方法签名未找到", methodName);
            throw new UpExcetion(40402, "方法签名未找到");
        }

        String[] restPaths = new String[5];
        boolean restPathFlag = false;
        // RESTful
        if (dubbo2MethodProperties.getRestPath() != null) {
            restPathFlag = true;
            restPaths = dubbo2MethodProperties.getRestPath().split("/");
        }
        List<Dubbo2ParamProperties> dubbo2ParamProperties = dubbo2MethodProperties.getParamList();
        // 处理请求类型
        for (int i = 0, uriI = 3, restPathI = 1; i < dubbo2ParamProperties.size(); i++) {
            // 参数列表
            Dubbo2ParamProperties param = dubbo2ParamProperties.get(i);
            // RESTful
            if (restPathFlag) {
                // 按顺序拿uri中的值添加到列表
                String pathVariable = uris[uriI];
                if (restPaths[restPathI].startsWith("{") || restPaths[restPathI].endsWith("}")) {
                    // set Value
                    paramList.add(pathVariable);
                    uriI++;
                }
            } else {
                // set Value
                paramList.add(httpServletRequest.getParameter(param.getFiled()));
            }
            // set Type 按顺序
            paramTypeList.add(Dubbo2ParamTypeEnum.valueOf(param.getType()).getJavaType());
        }
        serviceInfo.setParams(paramList);
        serviceInfo.setParamTypes(paramTypeList);
        return serviceInfo;
    }


}
