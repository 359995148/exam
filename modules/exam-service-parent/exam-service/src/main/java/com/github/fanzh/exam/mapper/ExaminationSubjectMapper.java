package com.github.fanzh.exam.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.github.fanzh.exam.api.module.ExaminationSubject;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考试题目关联mapper
 *
 * @author fanzh
 * @date 2019/6/16 15:37
 */
@Mapper
public interface ExaminationSubjectMapper extends BaseMapper<ExaminationSubject> {


}
