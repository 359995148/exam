package com.github.fanzh.exam.api.feign.fallback;

import com.github.fanzh.exam.api.dto.ExaminationDashboardDto;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.exam.api.feign.ExaminationServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 考试服务熔断
 *
 * @author fanzh
 * @date 2019-03-01 15:22
 */
@Slf4j
@Service
public class ExaminationServiceClientFallbackImpl implements ExaminationServiceClient {

	private Throwable throwable;

	@Override
	public ExecResult<ExaminationDashboardDto> findExaminationDashboardData(String tenantCode) {
		log.error("Call findExaminationDashboardData error, {}", tenantCode, throwable);
		return new ExecResult<>(new ExaminationDashboardDto());
	}

	@Override
	public ExecResult<ExaminationDashboardDto> findExamRecordTendencyData(String tenantCode, Integer pastDays) {
		log.error("Call findExamRecordTendencyData error, {}, {}", tenantCode, pastDays, throwable);
		return new ExecResult<>(new ExaminationDashboardDto());
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}
}
