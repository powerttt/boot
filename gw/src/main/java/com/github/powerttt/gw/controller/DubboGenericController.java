package com.github.powerttt.gw.controller;

import com.alibaba.fastjson.JSON;
import com.github.powerttt.commons.ResultBean;
import com.github.powerttt.commons.exception.UpExcetion;
import com.github.powerttt.gw.config.Dubbo2RouterProperties;
import com.github.powerttt.gw.entity.*;
import com.github.powerttt.gw.enums.Dubbo2ParamTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class DubboGenericController {

    @Autowired
    private Dubbo2RouterProperties dubbo2RouterProperties;

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private RegistryConfig registryConfig;

    @Value("dubbo.defaultVersion")
    private String defaultVersion;

    @RequestMapping
    public Object dubboExec(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            long startTime = System.currentTimeMillis();
            ServiceInfo serviceInfo = generateInterfaceInfo(httpServletRequest, httpServletResponse);
            log.info("{}  服务信息： {}", httpServletRequest.getParameter("auth"), JSON.toJSONString(serviceInfo));
            Object result = dubboInvoker(serviceInfo);
            log.info("result : {} ", JSON.toJSONString(result));
            log.info("user time : {} ", System.currentTimeMillis() - startTime);
            return result;
        } catch (UpExcetion upExcetion) {
            return new ResultBean().failure(upExcetion.getCode(), upExcetion.getMsg());
        }
    }

    private Object dubboInvoker(ServiceInfo serviceInfo) {

        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(serviceInfo.getServerName());
        referenceConfig.setGeneric(true);
        referenceConfig.setVersion(serviceInfo.getVersion());
        referenceConfig.setApplication(applicationConfig);
        referenceConfig.setRegistry(registryConfig);
        referenceConfig.setTimeout(1000);
        referenceConfig.setRetries(0);
        /*
        ReferenceConfig 实例很重，封装了与注册中心的连接以及与提供者的连接，需要缓存。
        否则重复生成 ReferenceConfig 可能造成性能问题并且会有内存和连接泄漏。在 API 方式编程时，容易忽略此问题。
         */
        ReferenceConfigCache cache = ReferenceConfigCache.getCache();
        // cache.get方法中会缓存 Reference对象，并且调用ReferenceConfig.get方法启动ReferenceConfig
        GenericService genericService = cache.get(referenceConfig);
        // 注意！ Cache会持有ReferenceConfig，不要在外部再调用ReferenceConfig的destroy方法，导致Cache内的ReferenceConfig失效！
        if (genericService == null) {
            cache.destroy(referenceConfig);
            throw new IllegalStateException("服务不可用");
        }
        return genericService.$invoke(serviceInfo.getMethodName(), serviceInfo.getParamTypes().toArray(new String[0]), serviceInfo.getParams().toArray());
    }

    /**
     * 获取请求中的参数
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @return
     * @throws UpExcetion
     */
    private ServiceInfo generateInterfaceInfo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws UpExcetion {
        String uri = httpServletRequest.getRequestURI();
        log.info("URI : {}", uri);
        String[] uris = uri.substring(1).split("/");
        // 请求格式有误
        if (uris.length < 2) {
            log.warn("URI ：{}  解析错误", uri);
            throw new UpExcetion(40401, "URI解析错误");
        }
        String serviceName = uris[0];
        Dubbo2InterfaceProperties dubbo2InterfaceProperties = dubbo2RouterProperties.getRouters().get(serviceName);
        if (dubbo2InterfaceProperties == null) {
            log.warn("interfaceName ：{}  未找到此服务配置", serviceName);
            throw new UpExcetion(40402, "未找到此服务配置");
        }
        String methodName = uris[1];
        if (StringUtils.isEmpty(methodName)) {
            log.warn("method ：{}  解析错误", uri);
        }
        assert dubbo2InterfaceProperties != null;

        Dubbo2MethodProperties dubbo2MethodProperties = dubbo2InterfaceProperties.getMethods().get(methodName);
        if (dubbo2MethodProperties == null) {
            if (dubbo2InterfaceProperties == null) {
                log.warn("method ：{}  未找到此服务方法", methodName);
                throw new UpExcetion(40402, "未找到此服务配置");
            }
        }
        if (null == dubbo2MethodProperties) {
            log.warn("method ：{}  请配置请求方法", methodName);
            throw new UpExcetion(40402, "未找到配置的服务方法");
        }
        // 校验请求是否对应
        if (!httpServletRequest.getMethod().equals(dubbo2MethodProperties.getReqMethod())) {
            log.warn("Http method ：{}  请求动作错误， conf: {}", httpServletRequest.getMethod(), dubbo2MethodProperties.getReqMethod());
            throw new UpExcetion(40402, "未找到配置的服务方法");
        }
        // 获取方法info
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setServerName(dubbo2InterfaceProperties.getServerName());
        serviceInfo.setMethodName(methodName);
        String version = httpServletRequest.getParameter("version");
        serviceInfo.setVersion(version == null ? defaultVersion : version);
        List<String> paramTypeList = new ArrayList<>();
        List<Object> paramList = new ArrayList<>();

        if (dubbo2MethodProperties.getParamList() == null) {
            log.warn("method ：{}  方法签名未找到", methodName);
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
