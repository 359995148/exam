package com.github.fanzh.user.controller;


import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.user.service.MobileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 手机管理Controller
 *
 * @author fanzh
 * @date 2019/07/02 09:34
 */
@Slf4j
@AllArgsConstructor
@Api("手机管理")
@RestController
@RequestMapping("/v1/mobile")
public class MobileController {

    private final MobileService mobileService;

    /**
     * 发送短信
     *
     * @param mobile mobile
     * @return ResponseBean
     * @author fanzh
     * @date 2019/07/02 09:49:05
     */
    @ApiImplicitParam(name = "mobile", value = "mobile", required = true, dataType = "String", paramType = "path")
    @ApiOperation(value = "发送短信", notes = "发送短信到指定的手机号")
    @GetMapping("sendSms/{mobile}")
    public ExecResult<Boolean> sendSms(@PathVariable String mobile) {
        return mobileService.sendSms(mobile);
    }
}
