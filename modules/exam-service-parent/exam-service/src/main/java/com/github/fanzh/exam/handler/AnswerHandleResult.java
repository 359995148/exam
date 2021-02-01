package com.github.fanzh.exam.handler;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 计算结果
 * @author fanzh
 * @date 2019/12/8 9:56 下午
 */
@Data
public class AnswerHandleResult {

	/**
	 * 总分
	 */
	private BigDecimal score;

	/**
	 * 正确题目数
	 */
	private int correctNum;

	/**
	 * 错误题目数
	 */
	private int inCorrectNum;
}
