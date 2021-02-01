package com.github.fanzh.user.service;

import cn.hutool.core.util.RandomUtil;
import com.github.fanzh.common.core.enums.LoginTypeEnum;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.exceptions.ServiceException;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.security.constant.SecurityConstant;
import com.github.fanzh.msc.api.constant.SmsConstant;
import com.github.fanzh.msc.api.dto.SmsDto;
import com.github.fanzh.msc.api.feign.MscServiceClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 手机管理Service
 *
 * @author fanzh
 * @date 2019/07/02 09:35
 */
@Slf4j
@AllArgsConstructor
@Service
public class MobileService {

    private final RedisTemplate redisTemplate;

    private final MscServiceClient mscServiceClient;

    /**
     * 发送短信
     *
     * @param mobile     mobile
     * @return ResponseBean
     * @author fanzh
     * @date 2019/07/02 09:36:52
     */
    public ExecResult<Boolean> sendSms(String mobile) {
        String key = CommonConstant.DEFAULT_CODE_KEY + LoginTypeEnum.SMS.getType() + "@" + mobile;
        String code = RandomUtil.randomNumbers(Integer.parseInt(CommonConstant.CODE_SIZE));
        log.debug("Generate validate code success: {}, {}", mobile, code);
        redisTemplate.opsForValue().set(key, code, SecurityConstant.DEFAULT_SMS_EXPIRE, TimeUnit.SECONDS);
        // 调用消息中心服务，发送短信验证码
        SmsDto smsDto = new SmsDto();
        smsDto.setReceiver(mobile);
        smsDto.setContent(String.format(SmsConstant.SMS_TEMPLATE, code));
        ExecResult<?> result = mscServiceClient.sendSms(smsDto);
        if (result.isError()) {
            throw new ServiceException("Send validate code error: " + result.getMsg());
        }
        log.info("Send validate result: {}", result.getData());
        return ExecResultUtil.success(true);
    }
}
