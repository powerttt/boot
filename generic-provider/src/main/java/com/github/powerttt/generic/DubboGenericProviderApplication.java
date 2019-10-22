package com.github.powerttt.generic;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author tongning
 * @Date 2019/7/13 0013
 * function:<
 * <p>
 * >
 */
@SpringBootApplication
@EnableDubbo
public class DubboGenericProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(DubboGenericProviderApplication.class, args);
    }
}
