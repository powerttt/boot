package com.github.boot.commons.constants;

import java.util.function.Function;

/**
 * @author: tn
 * @Date: 2020/1/2 0002 15:58
 * @Description:
 */
public class RedisKey {

    public static final String UN_READ_MESSAGE = "UN_READ_MESSAGE::";

    public static Function<String, String> getMessageKey = userNo -> UN_READ_MESSAGE + userNo;


}
