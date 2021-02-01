package com.github.fanzh.user.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.github.fanzh.user.api.module.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色mapper
 *
 * @author fanzh
 * @date 2018/8/26 09:33
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

}
