package com.github.boot.gw.controller;

import com.github.boot.gw.entity.ServiceInfo;
import com.github.boot.commons.constants.JwtKeys;
import com.github.boot.commons.exception.UpException;
import com.github.boot.commons.result.ResultBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    public ResultBean dubboExec(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            long startTime = System.currentTimeMillis();
            ServiceInfo serviceInfo = (ServiceInfo) httpServletRequest.getAttribute(ServiceInfo.class.getSimpleName());
            if (serviceInfo == null) {
                log.warn("ServerInfoJson is null");
                return new ResultBean().failure("401001", "ServerInfoJson is null");
            }
            String version = httpServletRequest.getParameter("version");
            serviceInfo.setVersion(version == null ? defaultVersion : version);
            String userno = (String) serviceInfo.getClaims().get(JwtKeys.RPC_USER_NO);
            // 注入rpc参数
            serviceInfo.getClaims().forEach((k, v) -> RpcContext.getContext().setAttachment(k, String.valueOf(v)));
            Object result = null;
            try {
                result = dubboInvoker(serviceInfo);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResultBean().failure("400000", "服务异常");
            }
            if (userno != null) {
                log.info("{} used time: \u001b[31m{}\u001b[0m ms [\u001b[31m{}.{}\u001b[0m]", userno, System.currentTimeMillis() - startTime, serviceInfo.getServerName(), serviceInfo.getMethodName());
            } else {
                log.info("demo used time: \u001b[31m{}\u001b[0m ms [\u001b[31m{}.{}\u001b[0m]", System.currentTimeMillis() - startTime, serviceInfo.getServerName(), serviceInfo.getMethodName());
            }

            return result instanceof ResultBean ? (ResultBean) result : new ResultBean().success(result);
        } catch (UpException upException) {
            upException.printStackTrace();
            return new ResultBean().failure(upException.getCode(), upException.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
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
        referenceConfig.setTimeout(100000);
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
        if (serviceInfo.getParams() == null || serviceInfo.getParams().size() == 0) {
            return genericService.$invoke(serviceInfo.getMethodName(), null, null);
        }
        return genericService.$invoke(serviceInfo.getMethodName(), serviceInfo.getParamTypes().toArray(new String[0]), serviceInfo.getParams().toArray());
    }
}
