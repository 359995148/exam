package com.github.fanzh.user.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.github.fanzh.user.api.module.Dept;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单mapper
 *
 * @author fanzh
 * @date 2018/8/26 22:34
 */
@Mapper
public interface DeptMapper extends BaseMapper<Dept> {
}
