package com.github.fanzh.user.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.github.fanzh.common.core.model.Log;
import org.apache.ibatis.annotations.Mapper;

/**
 * 日志
 *
 * @author fanzh
 * @date 2018/10/31 20:38
 */
@Mapper
public interface LogMapper extends BaseMapper<Log> {
}
