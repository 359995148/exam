package com.github.fanzh.exam.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.github.fanzh.exam.api.module.Knowledge;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库Mapper
 *
 * @author fanzh
 * @date 2019/1/1 15:03
 */
@Mapper
public interface KnowledgeMapper extends BaseMapper<Knowledge> {
}
