package com.github.fanzh.exam.mq;

import com.github.fanzh.common.core.constant.MqConstant;
import com.github.fanzh.common.security.tenant.TenantContextHolder;
import com.github.fanzh.exam.api.enums.SubmitStatusEnum;
import com.github.fanzh.exam.api.module.Answer;
import com.github.fanzh.exam.api.module.ExaminationRecord;
import com.github.fanzh.exam.service.AnswerService;
import com.github.fanzh.exam.service.ExamRecordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 提交考试消息消费者
 *
 * @author fanzh
 * @date 2019/5/3 14:55
 */
@Slf4j
@AllArgsConstructor
@Service
public class RabbitSubmitExaminationReceiver {

    private final AnswerService answerService;

    private final ExamRecordService examRecordService;

    /**
     * 处理提交考试逻辑：统计错题，计算成绩等
     * 1. 先更新考试记录的状态为正在计算
     * 2. 更新成功则执行提交逻辑
     *
     * @param answer answer
     * @author fanzh
     * @date 2019/05/03 14:56
     */
    @RabbitListener(queues = {MqConstant.SUBMIT_EXAMINATION_QUEUE})
    public void submitExamination(Answer answer) {
        log.debug("examRecordId: {}, modifier: {}", answer.getExamRecordId(), answer.getModifier());
        // 异步提交会丢失tenantCode，需要手动设置
        TenantContextHolder.setTenantCode(answer.getTenantCode());
        Optional<ExaminationRecord> opt = examRecordService.baseFindById(answer.getExamRecordId());
        if (!opt.isPresent()) {
            return;
        }
        ExaminationRecord examRecord = opt.get();
        if (SubmitStatusEnum.NOT_SUBMITTED.getValue().equals(examRecord.getSubmitStatus())) {
            log.warn("Examination: {} not submitted", examRecord.getId());
        }
        if (SubmitStatusEnum.CALCULATE.getValue().equals(examRecord.getSubmitStatus())) {
            log.warn("Examination: {} is counting, please do not submit again", examRecord.getId());
        }
        // 更新状态为正在统计
        examRecord.setSubmitStatus(SubmitStatusEnum.CALCULATE.getValue());
        // 更新成功
        examRecordService.baseSave(examRecord);
        answerService.submit(answer);
    }
}
