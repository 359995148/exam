package com.github.fanzh.user.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.github.fanzh.user.api.module.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生Mapper
 *
 * @author fanzh
 * @date 2019/07/09 15:27
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
