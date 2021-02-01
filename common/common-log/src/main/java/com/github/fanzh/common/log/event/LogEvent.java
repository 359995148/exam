package com.github.fanzh.common.log.event;

import com.github.fanzh.common.core.model.Log;
import org.springframework.context.ApplicationEvent;

/**
 * 日志事件
 *
 * @author fanzh
 * @date 2019/3/12 23:58
 */
public class LogEvent extends ApplicationEvent {
    public LogEvent(Log source) {
        super(source);
    }
}
