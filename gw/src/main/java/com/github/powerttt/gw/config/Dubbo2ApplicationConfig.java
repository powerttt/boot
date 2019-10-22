package com.github.powerttt.gw.config;


import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Dubbo2ApplicationConfig {

    @Value("${dubbo.application.name}")
    private String applicationName;
    @Value("${dubbo.registry.address}")
    private String dubboRegistryAddress;

    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(applicationName);
        return applicationConfig;
    }

    @Bean
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(this.dubboRegistryAddress);
        return registryConfig;
    }
}
