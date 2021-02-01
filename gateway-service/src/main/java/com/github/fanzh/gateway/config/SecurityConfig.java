package com.github.fanzh.gateway.config;

import com.github.fanzh.common.core.properties.FilterIgnoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * oauth2 client 配置
 * @author fanzh
 * @date 2020/3/7 3:25 下午
 */
@Configuration
@Import(FilterIgnoreProperties.class)
@EnableWebFluxSecurity
public class SecurityConfig {

	@Autowired
	private FilterIgnoreProperties filterIgnoreProperties;

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		String[] ignores = new String[filterIgnoreProperties.getUrls().size()];
		http.csrf().disable()
				.authorizeExchange()
				.pathMatchers(filterIgnoreProperties.getUrls().toArray(ignores)).permitAll()
				.anyExchange().authenticated();
		http.oauth2ResourceServer().jwt();

		http
				// in a frame because it set 'X-Frame-Options' to 'deny'.
				.headers()
				.frameOptions().disable();
		return http.build();
	}
}
