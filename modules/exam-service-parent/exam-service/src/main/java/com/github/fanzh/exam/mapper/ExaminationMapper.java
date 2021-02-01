package com.github.fanzh.exam.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.github.fanzh.exam.api.module.Examination;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考试Mapper
 *
 * @author fanzh
 * @date 2018/11/8 21:11
 */
@Mapper
public interface ExaminationMapper extends BaseMapper<Examination> {


}
