package com.github.fanzh.exam.api.module;

import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 考试题目关联
 *
 * @author fanzh
 * @date 2019/6/16 13:46
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "exam_examination_subject")
public class ExaminationSubject extends BaseEntity {

    /**
     * 考试ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long examinationId;

    /**
     * 分类ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long categoryId;

    /**
     * 题目ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long subjectId;

    /**
     * 题目类型，0：选择题，1：简答题，2：判断题，3：多选题
     */
    private Integer type;
}
