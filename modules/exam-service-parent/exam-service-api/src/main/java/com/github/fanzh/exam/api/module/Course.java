package com.github.fanzh.exam.api.module;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 课程
 *
 * @author fanzh
 * @date 2018/11/8 20:43
 */
@Data
@TableName(value = "exam_course")
public class Course extends BaseEntity {

    /**
     * 课程名称
     */
    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    /**
     * 学院
     */
    private String college;

    /**
     * 专业
     */
    private String major;

    /**
     * 老师
     */
    private String teacher;

    /**
     * 课程描述
     */
    private String courseDescription;

    /**
     * logoId
     */
    @TableField(exist = false)
    private Long logoId;

    /**
     * logo URL
     */
    @TableField(exist = false)
    private String logoUrl;
}
