package com.github.fanzh.exam.api.module;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;

/**
 * 选择题的选项
 *
 * @author fanzh
 * @date 2018/11/8 20:53
 */
@Data
@TableName(value = "exam_subject_option")
public class SubjectOption extends BaseEntity {

    /**
     * 选择题ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long subjectChoicesId;

    /**
     * 选项名称
     */
    private String optionName;

    /**
     * 选项内容
     */
    private String optionContent;

    /**
     * 是否正确
     */
    @TableField(exist = false)
    private String right;
}
