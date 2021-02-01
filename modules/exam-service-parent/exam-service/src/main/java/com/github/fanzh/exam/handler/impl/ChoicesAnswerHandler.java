package com.github.fanzh.exam.handler.impl;

import com.github.fanzh.exam.api.dto.SubjectDto;
import com.github.fanzh.exam.api.module.Answer;
import com.github.fanzh.exam.enums.SubjectTypeEnum;
import com.github.fanzh.exam.api.constants.AnswerConstant;
import com.github.fanzh.exam.handler.AbstractAnswerHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 选择题
 * @author fanzh
 * @date 2019/12/8 21:57
 */
@Slf4j
@AllArgsConstructor
@Component
public class ChoicesAnswerHandler extends AbstractAnswerHandler {

	@Override
	public SubjectTypeEnum getSubjectType() {
		return SubjectTypeEnum.CHOICES;
	}

	@Override
	public boolean judgeRight(Answer answer, SubjectDto subject) {
		return subject.getAnswer().getAnswer().equalsIgnoreCase(answer.getAnswer());
	}

	/**
	 * 判断选项是否正确
	 *
	 * @param answer  answer
	 * @param subject subject
	 * @author fanzh
	 * @date 2020/02/19 23:23
	 */
	public void judgeOptionRight(Answer answer, SubjectDto subject) {
		String userAnswer = answer.getAnswer();
		String answerStr = subject.getAnswer().getAnswer();
		if (StringUtils.isNotBlank(userAnswer)) {
			subject.getOptions().forEach(option -> {
				if (userAnswer.equals(option.getOptionName())) {
					option.setRight(answerStr.equals(option.getOptionName()) ? TRUE : FALSE);
				}
			});
		}
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
