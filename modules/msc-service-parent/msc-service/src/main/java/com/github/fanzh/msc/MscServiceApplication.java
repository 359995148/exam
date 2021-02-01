package com.github.fanzh.msc;

import com.github.fanzh.common.basic.config.MybatisPlusConfig;
import com.github.fanzh.common.core.constant.CommonConstant;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;


@SpringBootApplication(
        scanBasePackages = {CommonConstant.BASE_PACKAGE}
        , exclude = {DataSourceAutoConfiguration.class
        , MybatisAutoConfiguration.class}
)
@ComponentScan(
        basePackages = CommonConstant.BASE_PACKAGE
        , excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {MybatisPlusConfig.class})}
)
@EnableDiscoveryClient
// 扫描api包里的FeignClient
@EnableFeignClients(basePackages = {CommonConstant.BASE_PACKAGE})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableCircuitBreaker
public class MscServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MscServiceApplication.class, args);
    }

}
