package com.github.fanzh.exam.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.fanzh.exam.mapper.ExamRecordMapper;
import com.github.fanzh.common.basic.vo.DeptVo;
import com.github.fanzh.common.basic.vo.UserVo;
import com.github.fanzh.common.core.constant.SqlField;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.core.utils.DateUtils;
import com.github.fanzh.common.basic.utils.EntityWrapperUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.exam.api.dto.AnswerDto;
import com.github.fanzh.exam.api.dto.ExaminationDashboardDto;
import com.github.fanzh.exam.api.dto.ExaminationRecordDto;
import com.github.fanzh.exam.api.dto.SubjectDto;
import com.github.fanzh.exam.api.module.Answer;
import com.github.fanzh.exam.api.module.Examination;
import com.github.fanzh.exam.api.module.ExaminationRecord;
import com.github.fanzh.exam.utils.ExamRecordUtil;
import com.github.fanzh.user.api.feign.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 考试记录service
 *
 * @author fanzh
 * @date 2018/11/8 21:20
 */
@Slf4j
@Component
public class ExamRecordService extends BaseService<ExamRecordMapper, ExaminationRecord> {

    @Autowired
    private  UserServiceClient userServiceClient;
    @Autowired
    private  ExaminationService examinationService;
    @Autowired
    private  AnswerService answerService;
    @Autowired
    private  SubjectService subjectService;


    /**
     * 获取用户、部门相关信息
     *
     * @param examRecordDtoList examRecordDtoList
     * @param userIds           userIds
     */
    public void fillUserDept(List<ExaminationRecordDto> examRecordDtoList, Set<Long> userIds) {
        if (ParamsUtil.isEmpty(examRecordDtoList)) {
            return;
        }
        // 查询用户信息
        List<UserVo> userList = new ArrayList<>();
        List<DeptVo> deptList = new ArrayList<>();
        ExecResult<List<UserVo>> userRet = userServiceClient.findUserById(new ArrayList<>(userIds));
        if (userRet.isSuccess() && ParamsUtil.isNotEmpty(userRet.getData())) {
            userList.addAll(userRet.getData());
        } else {
            log.error("Method userServiceClient.findUserById() request failed: {}", userRet.getMsg());
        }
        // 查询部门信息
        ExecResult<List<DeptVo>> deptRet = userServiceClient.findDeptById(new ArrayList<>(userRet.getData().stream().map(o -> o.getDeptId()).collect(Collectors.toSet())));
        if (deptRet.isSuccess() && ParamsUtil.isNotEmpty(deptRet.getData())) {
            deptList.addAll(deptRet.getData());
        } else {
            log.error("Method userServiceClient.findDeptById() request failed: {}", deptRet.getMsg());
        }
        examRecordDtoList.forEach(tempExamRecordDto -> {
            // 查询、设置用户信息
            Optional<UserVo> userOpt = userList.stream()
                    .filter(tempUserVo -> tempExamRecordDto.getUserId().equals(tempUserVo.getId()))
                    .findAny();
            if (userOpt.isPresent()) {
                // 设置用户名
                tempExamRecordDto.setUserName(userOpt.get().getName());
                Optional<DeptVo> deptOpt = deptList.stream()
                        // 根据部门ID过滤
                        .filter(tempDept -> tempDept.getId().equals(userOpt.get().getDeptId()))
                        .findAny();
                if (deptOpt.isPresent()) {
                    // 设置部门名称
                    tempExamRecordDto.setDeptName(deptOpt.get().getDeptName());
                }
            }
        });
    }

    /**
     * 查询参与考试人数
     *
     * @return ExaminationDashboardDto
     * @author fanzh
     * @date 2019/10/27 20:07:38
     */
    public ExaminationDashboardDto findExamDashboardData(String tenantCode) {
        if (ParamsUtil.isEmpty(tenantCode)) {
            return new ExaminationDashboardDto();
        }
        ExaminationDashboardDto dashboardDto = new ExaminationDashboardDto();
        //考试数量
        EntityWrapper<Examination> examinationCountEw = EntityWrapperUtil.build();
        examinationCountEw.eq(SqlField.TENANT_CODE, tenantCode);
        dashboardDto.setExaminationCount(examinationService.selectCount(examinationCountEw));
        //考生数量
        EntityWrapper<ExaminationRecord> examUserCountEw = EntityWrapperUtil.build();
        examUserCountEw.eq(SqlField.TENANT_CODE, tenantCode);
        examUserCountEw.groupBy(SqlField.USER_ID);
        dashboardDto.setExamUserCount(this.selectList(examUserCountEw).size());
        // 考试记录数量
        EntityWrapper<ExaminationRecord> examinationRecordCountEw = EntityWrapperUtil.build();
        examinationRecordCountEw.eq(SqlField.TENANT_CODE, tenantCode);
        dashboardDto.setExaminationRecordCount(this.selectCount(examinationRecordCountEw));
        return dashboardDto;
    }

    /**
     * 查询过去n天的考试记录数据
     *
     * @param tenantCode tenantCode
     * @param pastDays   pastDays
     * @return ExaminationDashboardDto
     * @author fanzh
     * @date 2020/1/31 5:46 下午
     */
    public ExaminationDashboardDto findExamRecordTendency(String tenantCode, int pastDays) {
        ExaminationDashboardDto dashboardDto = new ExaminationDashboardDto();
        Examination examination = new Examination();
        examination.setCommonValue(SysUtil.getUser(), SysUtil.getSysCode(), tenantCode);
        Map<String, String> tendencyMap = new LinkedHashMap<>();
        LocalDateTime start = null;
        pastDays = -pastDays;
        for (int i = pastDays; i <= 0; i++) {
            LocalDateTime localDateTime = DateUtils.plusDay(i);
            if (i == pastDays) {
                start = localDateTime;
            }
            tendencyMap.put(localDateTime.format(DateUtils.FORMATTER_DAY), "0");
        }
        EntityWrapper<ExaminationRecord> ew = EntityWrapperUtil.build();
        ew.ge(SqlField.CREATE_DATE, DateUtils.asDate(start));
        List<ExaminationRecord> examinationRecords = this.selectList(ew);
        Map<String, List<ExaminationRecord>> examinationRecordsMap = examinationRecords.stream()
                .peek(examinationRecord -> examinationRecord
                        .setExt(DateUtils.asLocalDateTime(examinationRecord.getCreateDate())
                                .format(DateUtils.FORMATTER_DAY)))
                .collect(Collectors.groupingBy(ExaminationRecord::getExt));
        examinationRecordsMap.forEach((key, value) -> tendencyMap.replace(key, String.valueOf(value.size())));
        dashboardDto.setExamRecordDate(new ArrayList<>(tendencyMap.keySet()));
        dashboardDto.setExamRecordData(new ArrayList<>(tendencyMap.values()));
        return dashboardDto;
    }

    /**
     * 成绩详情
     *
     * @param id id
     * @return ExaminationRecordDto
     * @author fanzh
     * @date 2020/2/21 9:26 上午
     */
    public ExaminationRecordDto getExaminationRecordInfo(Long id) {
        ExaminationRecord examRecord = this.baseGetById(id);
        Examination examination = examinationService.baseGetById(examRecord.getExaminationId());
        ExaminationRecordDto examRecordDto = new ExaminationRecordDto();
        BeanUtils.copyProperties(examination, examRecordDto);
        examRecordDto.setId(examRecord.getId());
        examRecordDto.setStartTime(examRecord.getStartTime());
        examRecordDto.setEndTime(examRecord.getEndTime());
        examRecordDto.setScore(examRecord.getScore());
        examRecordDto.setUserId(examRecord.getUserId());
        examRecordDto.setExaminationId(examRecord.getExaminationId());
        examRecordDto.setDuration(ExamRecordUtil.getExamDuration(examRecord.getStartTime(), examRecord.getEndTime()));
        // 正确题目数
        examRecordDto.setCorrectNumber(examRecord.getCorrectNumber());
        examRecordDto.setInCorrectNumber(examRecord.getIncorrectNumber());
        // 提交状态
        examRecordDto.setSubmitStatus(examRecord.getSubmitStatus());
        // 答题列表
        List<Answer> answers = answerService.findAnswerByExamRecordId(examRecord.getId());
        if (CollectionUtils.isNotEmpty(answers)) {
            List<AnswerDto> answerDtos = answers.stream().map(answer -> {
                AnswerDto answerDto = new AnswerDto();
                BeanUtils.copyProperties(answer, answerDto);
                SubjectDto subjectDto = subjectService.get(answer.getSubjectId(), answer.getType());
                answerDto.setSubject(subjectDto);
                answerDto.setDuration(ExamRecordUtil.getExamDuration(answer.getStartTime(), answer.getEndTime()));
                return answerDto;
            }).collect(Collectors.toList());
            examRecordDto.setAnswers(answerDtos);
        }
        this.fillUserDept(Collections.singletonList(examRecordDto), SetUtil.build(examRecord.getUserId()));
        return examRecordDto;
    }
}
