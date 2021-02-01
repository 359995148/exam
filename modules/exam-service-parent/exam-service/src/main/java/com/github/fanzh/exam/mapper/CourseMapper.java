package com.github.fanzh.exam.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.github.fanzh.exam.api.module.Course;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程Mapper
 *
 * @author fanzh
 * @date 2018/11/8 21:10
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
