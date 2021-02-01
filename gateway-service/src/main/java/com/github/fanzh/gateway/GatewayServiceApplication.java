package com.github.fanzh.gateway;

import com.github.fanzh.common.core.constant.CommonConstant;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = CommonConstant.BASE_PACKAGE)
@MapperScan(basePackages = {CommonConstant.BASE_PACKAGE})
@SpringBootApplication(scanBasePackages = {CommonConstant.BASE_PACKAGE})
@EnableDiscoveryClient
@EnableCircuitBreaker
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
