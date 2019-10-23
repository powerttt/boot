package com.github.powerttt.gw.controller;

import com.alibaba.fastjson.JSON;
import com.github.powerttt.commons.result.ResultBean;
import com.github.powerttt.commons.exception.UpExcetion;
import com.github.powerttt.gw.config.Dubbo2RouterProperties;
import com.github.powerttt.gw.entity.*;
import com.github.powerttt.gw.enums.Dubbo2ParamTypeEnum;
import com.github.powerttt.gw.utils.GwConstants;
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
    private ApplicationConfig applicationConfig;

    @Autowired
    private RegistryConfig registryConfig;

    @Value("${dubbo.default-version}")
    private String defaultVersion;

    @RequestMapping
    public Object dubboExec(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            long startTime = System.currentTimeMillis();
            ServiceInfo serviceInfo = (ServiceInfo) httpServletRequest.getAttribute(ServiceInfo.class.getSimpleName());
            if (serviceInfo == null) {
                log.warn("ServerInfoJson is null");
                return new ResultBean().failure("401001", "ServerInfoJson is null");
            }
            String version = httpServletRequest.getParameter("version");
            serviceInfo.setVersion(version == null ? defaultVersion : version);
            log.info("dubboExec server info ： {}", JSON.toJSONString(serviceInfo));
            Object result = dubboInvoker(serviceInfo);
            log.info("dubboExec result : {} ", JSON.toJSONString(result));
            log.info("dubboExec user time : {}ms ", System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            return new ResultBean().failure("500", "失败");
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


}
