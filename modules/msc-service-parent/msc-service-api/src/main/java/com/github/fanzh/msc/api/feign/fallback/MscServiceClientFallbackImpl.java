package com.github.fanzh.msc.api.feign.fallback;

import com.github.fanzh.msc.api.dto.SmsDto;
import com.github.fanzh.msc.api.feign.MscServiceClient;
import com.github.fanzh.common.core.entity.ExecResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 消息中心服务断路器
 *
 * @author fanzh
 * @date 2019/07/02 16:09
 */
@Slf4j
@Component
public class MscServiceClientFallbackImpl implements MscServiceClient {

    private Throwable throwable;

    @Override
    public ExecResult<?> sendSms(SmsDto smsDto) {
        log.error("Feign send message failed: {}, {}, {}", smsDto.getReceiver(), smsDto.getContent(), throwable);
        return null;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
