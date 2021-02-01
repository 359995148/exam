package com.github.fanzh.exam.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.github.fanzh.exam.api.module.ExaminationRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考试记录Mapper
 *
 * @author fanzh
 * @date 2018/11/8 21:12
 */
@Mapper
public interface ExamRecordMapper extends BaseMapper<ExaminationRecord> {


}
