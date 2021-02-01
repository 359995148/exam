package com.github.fanzh.msc.api.feign;

import com.github.fanzh.common.core.constant.ServiceConstant;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.feign.config.CustomFeignConfig;
import com.github.fanzh.msc.api.dto.SmsDto;
import com.github.fanzh.msc.api.feign.factory.MscServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 消息中心服务
 *
 * @author fanzh
 * @date 2019/07/02 16:04
 */
@FeignClient(value = ServiceConstant.MSC_SERVICE, configuration = CustomFeignConfig.class, fallbackFactory = MscServiceClientFallbackFactory.class)
public interface MscServiceClient {

    /**
     * 发送短信
     *
     * @param smsDto smsDto
     * @return ResponseBean
     * @author fanzh
     * @date 2019/07/02 16:07:27
     */
    @PostMapping("/v1/sms/sendSms")
    ExecResult<?> sendSms(@RequestBody SmsDto smsDto);
}
