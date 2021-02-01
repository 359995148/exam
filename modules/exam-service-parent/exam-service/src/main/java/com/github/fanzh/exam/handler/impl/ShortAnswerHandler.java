package com.github.fanzh.exam.handler.impl;

import com.github.fanzh.exam.api.constants.AnswerConstant;
import com.github.fanzh.exam.api.dto.SubjectDto;
import com.github.fanzh.exam.api.module.Answer;
import com.github.fanzh.exam.enums.SubjectTypeEnum;
import com.github.fanzh.exam.handler.AbstractAnswerHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 简答题
 * @author fanzh
 * @date 2019/12/8 22:00
 */
@Slf4j
@AllArgsConstructor
@Component
public class ShortAnswerHandler extends AbstractAnswerHandler {

	@Override
	public SubjectTypeEnum getSubjectType() {
		return SubjectTypeEnum.SHORT_ANSWER;
	}

	@Override
	public boolean judgeRight(Answer answer, SubjectDto subject) {
		// TODO 暂时全匹配
		return subject.getAnswer().getAnswer().equals(answer.getAnswer());
	}

	@Override
	public void judge(Answer answer, SubjectDto subject, List<BigDecimal> rightScore) {
		if (judgeRight(answer, subject)) {
			rightScore.add(subject.getScore());
			answer.setAnswerType(AnswerConstant.RIGHT);
			answer.setScore(subject.getScore());
		} else {
			answer.setAnswerType(AnswerConstant.WRONG);
			answer.setScore(BigDecimal.ZERO);
		}
		answer.setMarkStatus(AnswerConstant.MARKED);
	}
}
