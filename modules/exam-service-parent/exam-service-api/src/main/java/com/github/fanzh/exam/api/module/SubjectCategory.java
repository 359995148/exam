package com.github.fanzh.exam.api.module;

import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 题目分类
 *
 * @author fanzh
 * @date 2018-12-04 11:18
 */
@Data
@TableName(value = "exam_subject_category")
public class SubjectCategory extends BaseEntity {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    /**
     * 分类描述
     */
    private String categoryDesc;

    /**
     * 父分类id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long parentId;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 类型: 0-私共,1-公有
     */
    private Integer type;
}
