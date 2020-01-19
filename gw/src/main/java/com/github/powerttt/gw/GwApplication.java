package com.github.powerttt.gw;


import com.github.boot.commons.JWTUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @Author tongning
 * @Date 2019/7/13 0013
 * function:<
 * <p>
 * >
 */
@SpringBootApplication
public class GwApplication {
    public static void main(String[] args) {

        SpringApplication.run(GwApplication.class, args);
    }
    @Bean
    public JWTUtils jwtUtils() {
        return new JWTUtils();
    }
}
