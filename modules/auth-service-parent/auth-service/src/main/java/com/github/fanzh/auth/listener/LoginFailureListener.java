package com.github.fanzh.auth.listener;

import com.github.fanzh.common.security.event.CustomAuthenticationFailureEvent;
import com.github.fanzh.user.api.feign.UserServiceClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 *
 * 处理登录失败事件
 *
 * @author fanzh
 * @date 2019-11-11 23:52
 */
@Slf4j
@AllArgsConstructor
@Component
public class LoginFailureListener implements ApplicationListener<CustomAuthenticationFailureEvent> {

	private final UserServiceClient userServiceClient;

	@Override
	public void onApplicationEvent(CustomAuthenticationFailureEvent event) {
		// 登录失败后的处理

	}
}
