package com.github.fanzh.exam.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.github.fanzh.exam.api.module.SubjectCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目分类Mapper
 *
 * @author fanzh
 * @date 2018/12/4 21:48
 */
@Mapper
public interface SubjectCategoryMapper extends BaseMapper<SubjectCategory> {
}
