package com.github.fanzh.gateway.controller;

import com.github.fanzh.common.core.properties.SysProperties;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.utils.SpringContextHolder;
import com.github.fanzh.gateway.cache.loader.PreviewConfigLoader;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 演示环境开关配置
 * @author fanzh
 * @date 2019/12/15 18:56
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(value = "/api/preview")
public class PreviewController {

    private final SysProperties sysProperties;

    /**
     * 演示模式
     *
     * @param enable enable
     * @return ResponseBean
     * @author fanzh
     * @date 2019/12/15 19:45
     */
    @GetMapping("/enable")
    public ExecResult<Boolean> preview(@RequestParam(required = false) String enable, @RequestParam String secret) {

        if (StringUtils.isNotBlank(enable) && sysProperties.getGatewaySecret().equals(secret)) {
            log.info("Preview enable: {}", enable);
            RedisTemplate<String, String> redisTemplate = (RedisTemplate) SpringContextHolder.getApplicationContext().getBean("redisTemplate");
            redisTemplate.opsForValue().set(PreviewConfigLoader.PREVIEW_ENABLE, enable);
        }
        return new ExecResult<>(Boolean.TRUE);
    }

    /**
     * 获取演示模式开关
     *
     * @return ResponseBean
     * @author fanzh
     * @date 2019/12/15 19:45
     */
    @GetMapping("/getPreview")
    public ExecResult<String> getPreview() {
        RedisTemplate<String, String> redisTemplate = (RedisTemplate) SpringContextHolder.getApplicationContext().getBean("redisTemplate");
        return new ExecResult<>(redisTemplate.opsForValue().get(PreviewConfigLoader.PREVIEW_ENABLE));
    }
}
