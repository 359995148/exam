package com.github.fanzh.common.basic.config;

import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.properties.SysProperties;
import com.github.fanzh.common.core.utils.ParamsUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 系统启动时的一些处理
 *
 * @author fanzh
 * @date 2019/07/14 16:09
 */
@Slf4j
@AllArgsConstructor
@Component
public class AppStartupRunner implements CommandLineRunner {

    private final SysProperties sysProperties;

    @Override
    public void run(String... args) throws Exception {
        // 设置系统属性
        if (ParamsUtil.isNotEmpty(sysProperties.getCacheExpire())) {
            System.setProperty(CommonConstant.CACHE_EXPIRE, sysProperties.getCacheExpire());
        }
    }
}
