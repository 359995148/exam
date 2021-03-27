package com.github.fanzh.msc.controller;


import com.github.fanzh.msc.api.dto.SmsDto;
import com.github.fanzh.msc.api.model.SmsResponse;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.msc.service.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发送短信接口
 *
 * @author fanzh
 * @date 2019/6/22 12:59
 */
@Slf4j
@AllArgsConstructor
@Api("发送短信")
@RestController
@RequestMapping(value = "/v1/sms")
public class SmsController {

    private final SmsService smsService;

    /**
     * 发送短信
     *
     * @param smsDto smsDto
     * @return ResponseBean
     * @author fanzh
     * @date 2019/06/22 13:12
     */
    @ApiOperation(value = "发送短信")
    @PostMapping("sendSms")
    public ExecResult<SmsResponse> sendSms(@RequestBody SmsDto smsDto) {
        log.info("Send message to {}, content: {}", smsDto.getReceiver(), smsDto.getContent());
        SmsResponse smsResponse = smsService.sendSms(smsDto);
        log.info("Send message success, response: {}", smsResponse);
        return new ExecResult<>(smsResponse);
    }
}
