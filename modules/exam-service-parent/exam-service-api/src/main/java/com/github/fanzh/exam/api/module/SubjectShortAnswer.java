package com.github.fanzh.exam.api.module;

import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 简答题
 *
 * @author fanzh
 * @date 2018/11/8 20:53
 */
@Data
@TableName(value = "exam_subject_short_answer")
public class SubjectShortAnswer extends BaseEntity {

	/**
	 * 题目分类ID
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private Long categoryId;

	/**
	 * 题目名称
	 */
	@NotBlank(message = "题目名称不能为空")
	private String subjectName;

	/**
	 * 参考答案
	 */
	private String answer;

	/**
	 * 分值
	 */
	@NotBlank(message = "题目分值不能为空")
	private BigDecimal score;

	/**
	 * 解析
	 */
	private String analysis;

	/**
	 * 难度等级
	 */
	private Integer level;
}
