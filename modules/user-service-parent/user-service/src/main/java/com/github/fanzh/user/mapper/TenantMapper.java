package com.github.fanzh.user.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.github.fanzh.user.api.module.Tenant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户Mapper
 *
 * @author fanzh
 * @date 2019/5/22 22:50
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {

}
