package com.github.fanzh.user.controller;

import com.github.fanzh.common.core.model.SysConfig;
import com.github.fanzh.common.core.properties.SysProperties;
import com.github.fanzh.common.core.entity.ExecResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统配置controller
 *
 * @author fanzh
 * @date 2019-02-28 17:29
 */
@AllArgsConstructor
@Api("系统配置信息管理")
@RestController
@RequestMapping("/v1/sysConfig")
public class SysConfigController {

    private final SysProperties sysProperties;

    /**
     * 获取系统配置
     *
     * @return ResponseBean
     * @author fanzh
     * @date 2019/2/28 17:31
     */
    @ApiOperation(value = "获取系统配置", notes = "获取系统配置")
    @GetMapping
    public ExecResult<SysConfig> getSysConfig() {
        SysConfig sysConfig = new SysConfig();
        BeanUtils.copyProperties(sysProperties, sysConfig);
        return new ExecResult<>(sysConfig);
    }
}
