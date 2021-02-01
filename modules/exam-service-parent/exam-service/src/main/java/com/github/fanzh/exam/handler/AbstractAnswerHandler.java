package com.github.fanzh.exam.handler;

import com.github.fanzh.exam.api.dto.SubjectDto;
import com.github.fanzh.exam.api.module.Answer;
import com.github.fanzh.exam.service.ISubjectService;
import com.github.fanzh.exam.service.SubjectService;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.SpringContextHolder;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计成绩
 *
 * @author fanzh
 * @date 2020/1/19 10:07 上午
 */
public abstract class AbstractAnswerHandler implements IAnswerHandler {

    @Override
    public AnswerHandleResult handle(List<Answer> answers) {
        if (CollectionUtils.isNotEmpty(answers)) {
            // 保存答题正确的题目分数
            List<BigDecimal> rightScore = new ArrayList<>();
            // 获取题目信息
            List<SubjectDto> subjects = getSubjects(answers);
            answers.forEach(tempAnswer -> {
                subjects.stream()
                        // 题目ID匹配
                        .filter(tempSubject -> tempSubject.getId().equals(tempAnswer.getSubjectId())).findFirst()
                        .ifPresent(subject -> judge(tempAnswer, subject, rightScore));
            });
            AnswerHandleResult result = new AnswerHandleResult();
            // 记录总分、正确题目数、错误题目数
            result.setScore(rightScore.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
            result.setCorrectNum(rightScore.size());
            result.setInCorrectNum(answers.size() - rightScore.size());
            return result;
        }
        return null;
    }

    @Override
    public List<SubjectDto> getSubjects(List<Answer> answers) {
        SubjectService subjectService = SpringContextHolder.getApplicationContext().getBean(SubjectService.class);
        Map<Integer, List<Answer>> answersGroup = answers.stream().collect(Collectors.groupingBy(o -> o.getType()));
        List<SubjectDto> list = new ArrayList<>();
        for (Map.Entry<Integer, List<Answer>> entry : answersGroup.entrySet()) {
            ISubjectService service = subjectService.subjectService(entry.getKey());
            if (ParamsUtil.isEmpty(service)) {
                continue;
            }
            list.addAll(service.findSubject(entry.getValue().stream().map(o -> o.getSubjectId()).collect(Collectors.toSet())));
        }
        return list;
    }
}
