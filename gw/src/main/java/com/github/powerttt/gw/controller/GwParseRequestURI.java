package com.github.powerttt.gw.controller;

import com.github.powerttt.commons.exception.UpException;
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
import java.io.BufferedReader;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 解析请求服务方法工具类
 */
@Slf4j
@Component
public class GwParseRequestURI {

    @Autowired
    private Dubbo2RouterProperties dubbo2RouterProperties;

    /**
     * 获取请求中的参数
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @return
     * @throws UpException
     */
    public ServiceInfo generateInterfaceInfo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        String uri = httpServletRequest.getRequestURI();
        log.info("generateInterfaceInfo URI : {}", uri);
        String[] uris = uri.substring(1).split("/");
        // 请求格式有误
        if (uris.length < 2) {
            log.warn("generateInterfaceInfo URI ：{}  解析错误", uri);
            throw new UpException(40401, "请求格式有误");
        }
        String serviceName = uris[0];
        String methodName = uris[1];

        Dubbo2InterfaceProperties dubbo2InterfaceProperties = Optional.ofNullable(dubbo2RouterProperties.getRouters().get(serviceName)).orElseThrow(() -> new UpException(40402, "未找到服务"));


        Dubbo2MethodProperties dubbo2MethodProperties = Optional.ofNullable(dubbo2InterfaceProperties.getMethods().get(methodName)).orElseThrow(() -> new UpException(40402, "未找到服务的方法"));

        // 校验请求是否对应
        if (!dubbo2MethodProperties.getReqMethod().contains(httpServletRequest.getMethod())) {
            log.warn("generateInterfaceInfo Http method ：{}  请求动作错误， conf: {}", httpServletRequest.getMethod(), dubbo2MethodProperties.getReqMethod());
            throw new UpException(40402, "未找到配置的服务方法");
        }
        // 获取方法info
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setServerName(dubbo2InterfaceProperties.getServerName());
        serviceInfo.setMethodName(methodName);

        List<Dubbo2ParamProperties> paramProperties = dubbo2MethodProperties.getParamList();
        if (paramProperties == null) {
            return serviceInfo;
        }
        List<String> paramTypeList = new ArrayList<>();
        List<Object> paramList = new ArrayList<>();

        Map<String, String[]> parameterMap = new HashMap<>(httpServletRequest.getParameterMap());


        // rest     learnData/{type}/{id} uris[] = {"learnData","1","1"}}
        if (dubbo2MethodProperties.getRestPath() != null) {
            List<String> restParamList = new ArrayList<>();
            String[] propertiesUris = dubbo2MethodProperties.getRestPath().split("/");
            // uri               => /user/getList/1   uriJ = 1
            // propertiesUris    => getList/1         i = 0
            for (int i = 0, uriJ = 1; i < propertiesUris.length; i++, uriJ++) {
                if (propertiesUris[i].contains("{") || propertiesUris[i].contains("}")) {
                    parameterMap.put(StringUtils.substringBetween(propertiesUris[i], "{", "}"), new String[]{uris[uriJ]});
                }
            }
        }
        // 获取put请求
        if ("PUT".equals(httpServletRequest.getMethod()) || "POST".equals(httpServletRequest.getMethod())) {
            if (parameterMap.isEmpty()) parameterMap = generateBodyParams(httpServletRequest);
        }

        for (Dubbo2ParamProperties param : paramProperties) {
            Optional<String[]> strings = Optional.ofNullable(parameterMap.get(param.getFiled()));
            paramList.add(strings.<Object>map(value -> value[0]).orElse(null));
            paramTypeList.add(Dubbo2ParamTypeEnum.valueOf(param.getType()).getJavaType());
        }
        log.info("params: {}", paramList.toString());
        serviceInfo.setParams(paramList);
        serviceInfo.setParamTypes(paramTypeList);
        return serviceInfo;
    }

    /**
     * 限于本项目 结构 请求格式非json： ｛userId:1｝，无法直接转对象，故需要再转json 并且带有名字 user={"\/"userId"\":1}
     * 对于项目而言 表单仅视为String参数，并且不能携带 & 符号
     * @param httpServletRequest
     * @return
     * @throws Exception
     */
    private Map<String, String[]> generateBodyParams(HttpServletRequest httpServletRequest) throws Exception {
        StringBuffer data = new StringBuffer();
        String line = null;
        BufferedReader reader = null;
        reader = httpServletRequest.getReader();
        while (null != (line = reader.readLine())) {
            data.append(line);
        }
        String bodyParams = URLDecoder.decode(data.toString(), "UTF-8");
        log.info("body : {}", bodyParams);
        return Arrays.asList(bodyParams.split("&")).stream().map(param -> param.split("=")).collect(Collectors.toMap(param -> param[0], param -> new String[]{param[1]}));

    }
}
