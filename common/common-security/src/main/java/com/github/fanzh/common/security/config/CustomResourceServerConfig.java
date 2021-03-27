package com.github.fanzh.common.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fanzh.common.core.properties.FilterIgnoreProperties;
import com.github.fanzh.common.security.handler.CustomAccessDeniedHandler;
import com.github.fanzh.common.security.mobile.MobileSecurityConfigurer;
import com.github.fanzh.common.security.wx.WxSecurityConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * 资源服务器配置
 *
 * @author fanzh
 * @date 2019-03-15 11:37
 */
@Configuration
@EnableResourceServer
public class CustomResourceServerConfig extends ResourceServerConfigurerAdapter {

    private static final String RESOURCE_ID = "resource_id";

    /**
     * 开放权限的URL
     */
    private final FilterIgnoreProperties filterIgnoreProperties;

    /**
     * 手机登录配置
     */
    private final MobileSecurityConfigurer mobileSecurityConfigurer;

    /**
     * 微信登录配置
     */
    private final WxSecurityConfigurer wxSecurityConfigurer;

    private final ObjectMapper objectMapper;

    @Autowired
    public CustomResourceServerConfig(
            FilterIgnoreProperties filterIgnoreProperties
            , MobileSecurityConfigurer mobileSecurityConfigurer
            , WxSecurityConfigurer wxSecurityConfigurer
            , ObjectMapper objectMapper
    ) {
        this.filterIgnoreProperties = filterIgnoreProperties;
        this.mobileSecurityConfigurer = mobileSecurityConfigurer;
        this.wxSecurityConfigurer = wxSecurityConfigurer;
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(RESOURCE_ID).stateless(false);
        resources.accessDeniedHandler(accessDeniedHandler());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        String[] ignores = new String[filterIgnoreProperties.getUrls().size()];
        http    // 跨站请求伪造
                .csrf().disable()
                .httpBasic().disable()
                .authorizeRequests()
                // 跳过认证的url地址
                .antMatchers(filterIgnoreProperties.getUrls().toArray(ignores)).permitAll()
                // 拦截所有的请求, 必须认证
                .anyRequest().authenticated()
                // 异常处理 返回的信息
                .and().exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler())
        ;
        // 手机号登录
        http.apply(mobileSecurityConfigurer);
        // 微信登录
        http.apply(wxSecurityConfigurer);
    }

    @Bean
    @ConditionalOnMissingBean(AccessDeniedHandler.class)
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler(objectMapper);
    }
}
