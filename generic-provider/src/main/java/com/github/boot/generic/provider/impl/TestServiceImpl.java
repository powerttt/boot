package com.github.boot.generic.provider.impl;

import com.github.boot.generic.provider.TestService;
import org.apache.dubbo.config.annotation.Service;

/**
 * @Author tongning
 * @Date 2019/7/13 0013
 * function:<
 * <p>
 * >
 */
@Service(version = "1.0")
public class TestServiceImpl implements TestService {
    @Override
    public void echo(String say) {
        System.out.println(say);
    }

    @Override
    public String test1() {
        return "Generic Success not filed";
    }

    @Override
    public String test(String say, Double test) {

        return say + " Generic Success " + test;
//        throw new NullPointerException("kong");
    }

    @Override
    public String restful(Long id) {
        return id + " RESTful";
    }
}
