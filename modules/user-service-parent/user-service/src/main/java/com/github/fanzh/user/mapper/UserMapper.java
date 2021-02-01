package com.github.fanzh.user.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.github.fanzh.common.basic.vo.UserVo;
import com.github.fanzh.user.api.module.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户mapper接口
 *
 * @author fanzh
 * @date 2018-08-25 15:27
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询用户数量
     *
     * @param userVo userVo
     * @return Integer
     */
    Integer userCount(UserVo userVo);
}
