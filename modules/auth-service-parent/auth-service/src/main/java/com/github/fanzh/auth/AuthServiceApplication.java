package com.github.fanzh.auth;

import com.github.fanzh.common.core.constant.CommonConstant;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;

@ComponentScan(basePackages = CommonConstant.BASE_PACKAGE)
@MapperScan(basePackages = {CommonConstant.BASE_PACKAGE})
@SpringBootApplication(scanBasePackages = {CommonConstant.BASE_PACKAGE})
@EnableDiscoveryClient
@EnableAuthorizationServer
// 扫描api包里的FeignClient
@EnableFeignClients(basePackages = {CommonConstant.BASE_PACKAGE})
@EnableCircuitBreaker
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

}
