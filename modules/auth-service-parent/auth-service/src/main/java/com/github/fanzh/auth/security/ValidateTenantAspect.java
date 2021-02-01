package com.github.fanzh.auth.security;

import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.exceptions.ServiceException;
import com.github.fanzh.common.core.exceptions.TenantNotFoundException;
import com.github.fanzh.user.api.feign.UserServiceClient;
import com.github.fanzh.user.api.module.Tenant;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 *
 * 校验租户
 *
 * @author fanzh
 * @date 2019-11-12 20:14
 */
@AllArgsConstructor
@Aspect
@Component
public class ValidateTenantAspect {

	private final UserServiceClient userServiceClient;

	@Before("execution(* com.github.fanzh.auth.security.CustomUserDetailsServiceImpl.*AndTenantCode(..)) && args(tenantCode,..)")
	public void validateTenantCode(String tenantCode) throws TenantNotFoundException {
		// 获取tenantCode
		if (StringUtils.isBlank(tenantCode)) {
			throw new TenantNotFoundException("tenantCode cant not be null");
		}
		// 先获取租户信息
		ExecResult<Tenant> tenantExecResult = userServiceClient.findTenantByTenantCode(tenantCode);
		if (tenantExecResult.isError()) {
			throw new ServiceException("get tenant info failed: " + tenantExecResult.getMsg());
		}
		Tenant tenant = tenantExecResult.getData();
		if (tenant == null) {
			throw new TenantNotFoundException("tenant does not exist");
		}
	}
}
