package com.github.fanzh.exam.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.exam.enums.SubjectTypeEnum;
import com.github.fanzh.exam.mapper.AnswerMapper;
import com.github.fanzh.common.basic.vo.UserVo;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.constant.MqConstant;
import com.github.fanzh.common.core.constant.SqlField;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.exceptions.CommonException;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.core.utils.DateUtils;
import com.github.fanzh.common.basic.utils.EntityWrapperUtil;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.JsonMapper;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.exam.api.constants.AnswerConstant;
import com.github.fanzh.exam.api.dto.AnswerDto;
import com.github.fanzh.exam.api.dto.RankInfoDto;
import com.github.fanzh.exam.api.dto.StartExamDto;
import com.github.fanzh.exam.api.dto.SubjectDto;
import com.github.fanzh.exam.api.enums.SubmitStatusEnum;
import com.github.fanzh.exam.api.module.Answer;
import com.github.fanzh.exam.api.module.Examination;
import com.github.fanzh.exam.api.module.ExaminationRecord;
import com.github.fanzh.exam.api.module.ExaminationSubject;
import com.github.fanzh.exam.handler.AnswerHandleResult;
import com.github.fanzh.exam.handler.impl.ChoicesAnswerHandler;
import com.github.fanzh.exam.handler.impl.JudgementAnswerHandler;
import com.github.fanzh.exam.handler.impl.MultipleChoicesAnswerHandler;
import com.github.fanzh.exam.handler.impl.ShortAnswerHandler;
import com.github.fanzh.exam.utils.AnswerHandlerUtil;
import com.github.fanzh.exam.utils.ExamRecordUtil;
import com.github.fanzh.user.api.feign.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 答题service
 *
 * @author fanzh
 * @date 2018/11/8 21:17
 */
@Slf4j
@Component
public class AnswerService extends BaseService<AnswerMapper, Answer> {

    @Autowired
    private  UserServiceClient userServiceClient;

    @Autowired
    private  AmqpTemplate amqpTemplate;

    @Autowired
    private  SubjectService subjectService;

    @Autowired
    private  ExamRecordService examRecordService;

    @Autowired
    private  ExaminationService examinationService;

    @Autowired
    private  ExaminationSubjectService examinationSubjectService;

    @Autowired
    private  ChoicesAnswerHandler choicesHandler;

    @Autowired
    private  MultipleChoicesAnswerHandler multipleChoicesHandler;

    @Autowired
    private  JudgementAnswerHandler judgementHandler;

    @Autowired
    private  ShortAnswerHandler shortAnswerHandler;

    @Autowired
    private  RedisTemplate<String, String> redisTemplate;


    /**
     * 新增
     *
     * @param answer
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public ExecResult<Boolean> addAnswer(Answer answer) {
        // TODO: 2020/12/30 这里需要做幂等
        this.baseSave(answer);
        return ExecResultUtil.success(true);
    }

    /**
     * 更新
     *
     * @param answer
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public ExecResult<Boolean> updateAnswer(Answer answer) {
        answer.setAnswer(AnswerHandlerUtil.replaceComma(answer.getAnswer()));
        this.baseUpdate(answer);
        return ExecResultUtil.success(true);
    }

    /**
     * 批改
     *
     * @param answer
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public ExecResult<Boolean> markAnswer(Answer answer) {
        Answer oldAnswer = this.baseGetById(answer.getId());
        // 加分减分逻辑
        if (!oldAnswer.getAnswerType().equals(answer.getAnswerType())) {
            ExaminationRecord record = examRecordService.baseGetById(oldAnswer.getExamRecordId());
            BigDecimal oldScore = record.getScore();
            if (AnswerConstant.RIGHT.equals(answer.getAnswerType())) {
                // 加分
                record.setCorrectNumber(record.getIncorrectNumber() + 1);
                record.setIncorrectNumber(record.getIncorrectNumber() - 1);
                record.setScore(record.getScore().add(answer.getScore()));
            } else if (AnswerConstant.WRONG.equals(answer.getAnswerType())) {
                // 减分
                record.setCorrectNumber(record.getIncorrectNumber() - 1);
                record.setIncorrectNumber(record.getIncorrectNumber() + 1);
                record.setScore(record.getScore().subtract(answer.getScore()));
            } else {
                throw new CommonException("answerType is invalid");
            }
            examRecordService.baseSave(record);
            log.info("Update examinationRecord success, examRecordId: {}, oldScore: {}, newScore: {}"
                    , oldAnswer.getExamRecordId(), oldScore, record.getScore()
            );
        }
        answer.setAnswer(AnswerHandlerUtil.replaceComma(answer.getAnswer()));
        return updateAnswer(answer);
    }

    public Optional<Answer> findAnswerByExamRecordIdAndSubjectId(Long examRecordId, Long subjectId) {
        if (ParamsUtil.isEmpty(examRecordId) || ParamsUtil.isEmpty(subjectId)) {
            return Optional.empty();
        }
        EntityWrapper<Answer> ew = EntityWrapperUtil.build();
        ew.eq("exam_record_id", examRecordId);
        ew.eq("subject_id", subjectId);
        return selectList(ew).stream().findAny();
    }

    /**
     * 保存答题
     *
     * @param answerDto
     */
    @Transactional(rollbackFor = Throwable.class)
    public void saveAnswerDto(AnswerDto answerDto) {
        Answer answer = new Answer();
        BeanUtils.copyProperties(answerDto, answer);
        Optional<Answer> oldAnswerOpt = findAnswerByExamRecordIdAndSubjectId(answer.getExamRecordId(), answer.getSubjectId());
        if (oldAnswerOpt.isPresent()) {
            Answer oldAnswer = oldAnswerOpt.get();
            oldAnswer.setAnswer(answer.getAnswer());
            oldAnswer.setType(answer.getType());
            oldAnswer.setEndTime(oldAnswer.getModifyDate());
            baseSave(oldAnswer);
        } else {
            answer.setId(null);
            answer.setCommonValue(SysUtil.getUser(), SysUtil.getSysCode(), SysUtil.getTenantCode());
            answer.setMarkStatus(AnswerConstant.TO_BE_MARKED);
            answer.setAnswerType(AnswerConstant.WRONG);
            answer.setEndTime(answer.getModifyDate());
            baseSave(answer);
        }
    }

    /**
     * 保存答题，返回下一题信息
     *
     * @param nextType
     * @param nextSubjectId
     * @param nextSubjectType
     * @param answerDto
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public ExecResult<SubjectDto> saveAndNext(Integer nextType, Optional<Long> nextSubjectId, Optional<Integer> nextSubjectType, AnswerDto answerDto) {
        saveAnswerDto(answerDto);
        SubjectDto subjectDto = subjectAnswer(
                answerDto.getSubjectId()
                , answerDto.getExamRecordId()
                , nextType
                , nextSubjectId
                , nextSubjectType
        );
        return ExecResultUtil.success(subjectDto);
    }

    @Transactional(rollbackFor = Throwable.class)
    public SubjectDto subjectAnswer(Long subjectId, Long examRecordId, Integer nextType, Optional<Long> nextSubjectId, Optional<Integer> nextSubjectType) {
        // 查找考试记录
        ExaminationRecord examRecord = examRecordService.baseGetById(examRecordId);
        EntityWrapper<ExaminationSubject> ew = new EntityWrapper();
        ew.eq(SqlField.DEL_FLAG, CommonConstant.DEL_FLAG_NORMAL);
        ew.eq("examination_id", examRecord.getExaminationId());
        ew.eq("subject_id", subjectId);
        ew.orderDesc(Arrays.asList(SqlField.ID));
        Page<ExaminationSubject> page = examinationSubjectService.selectPage(new Page<>(1, 1), ew);
        if (ParamsUtil.isEmpty(page.getRecords())) {
            log.error("ExaminationSubject does not exist by subjectId: {}", subjectId);
            throw new CommonException(String.format("序号为[%s]的题目不存在", subjectId));
        }
        SubjectDto subject;
        if (nextSubjectId.isPresent()) {
            if (!nextSubjectType.isPresent()) {
                throw new CommonException("Method subjectAnswer() param nextSubjectType does not exist");
            }
            subject = subjectService.get(nextSubjectId.get(), nextSubjectType.get());
        } else {
            subject = subjectService.getNextByCurrentIdAndType(examRecord.getExaminationId(), subjectId, page.getRecords().get(0).getType(), nextType);
        }
        if (ParamsUtil.isEmpty(subject)) {
            log.error("Subject does not exist by subjectId: {}", subjectId);
            return null;
        }

        // 查找答题
        Optional<Answer> userAnswerOpt = findAnswerByExamRecordIdAndSubjectId(examRecordId, subject.getId());
        Answer userAnswer = userAnswerOpt.orElse(new Answer());
        // 设置答题
        subject.setAnswer(userAnswer);
        subject.setExaminationRecordId(examRecordId);
        return subject;
    }

    /**
     * 答卷提交
     *
     * @param answer
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public ExecResult<Boolean> submitAsync(Answer answer) {
        ExaminationRecord examRecord = new ExaminationRecord();
        examRecord.setId(answer.getExamRecordId());
        examRecord.setEndTime(examRecord.getCreateDate());
        examRecord.setSubmitStatus(SubmitStatusEnum.SUBMITTED.getValue());
        // 2. 更新考试状态
        examRecordService.baseUpdate(examRecord);
        // 1. 发送消息
        answer.setModifier(SysUtil.getUser());
        answer.setApplicationCode(SysUtil.getSysCode());
        answer.setTenantCode(SysUtil.getTenantCode());
        amqpTemplate.convertAndSend(MqConstant.SUBMIT_EXAMINATION_QUEUE, answer);
        return ExecResultUtil.success(true);
    }

    public Page<AnswerDto> answerInfoListOrPage(Long recordId, Optional<Integer> pageNum, Optional<Integer> pageSize, Optional<String> sort, Optional<String> order, Answer answer) {
        EntityWrapper<Answer> ew = new EntityWrapper<>();
        ew.eq(SqlField.DEL_FLAG, CommonConstant.DEL_FLAG_NORMAL);
        ew.eq("exam_record_id", recordId);
        if (ParamsUtil.isNotEmpty(answer.getId())) {
            ew.eq(SqlField.ID, answer.getId());
        }
        if (ParamsUtil.isNotEmpty(answer.getSubjectId())) {
            ew.eq("subject_id", answer.getSubjectId());
        }
        if (ParamsUtil.isNotEmpty(answer.getType())) {
            ew.eq(SqlField.TYPE, answer.getType());
        }
        if (ParamsUtil.isNotEmpty(answer.getAnswerType())) {
            ew.eq("answer_type", answer.getAnswerType());
        }
        if (ParamsUtil.isNotEmpty(answer.getMarkStatus())) {
            ew.eq("mark_status", answer.getMarkStatus());
        }
        if (ParamsUtil.isNotEmpty(answer.getTenantCode())) {
            ew.eq("tenant_code", answer.getTenantCode());
        }
        if (sort.isPresent()) {
            if (Objects.equals(order.orElse(CommonConstant.PAGE_ORDER_DEFAULT), CommonConstant.PAGE_ORDER_DEFAULT)) {
                ew.orderDesc(Arrays.asList(sort.get()));
            } else {
                ew.orderAsc(Arrays.asList(sort.get()));
            }
        }
        Page<Answer> page = new Page<>();
        if (pageNum.isPresent() && pageSize.isPresent()) {
            page = selectPage(new Page(pageNum.get(), pageSize.get()), ew);
        } else {
            page.setRecords(selectList(ew));
        }

        List<AnswerDto> answerDtos = page.getRecords().stream().map(tempAnswer -> {
            AnswerDto answerDto = new AnswerDto();
            BeanUtils.copyProperties(tempAnswer, answerDto);
            SubjectDto subjectDto = subjectService.get(tempAnswer.getSubjectId(), tempAnswer.getType());
            answerDto.setSubject(subjectDto);
            // 判断正误
            SubjectTypeEnum subjectType = SubjectTypeEnum.matchByValue(subjectDto.getType());
            if (subjectType != null) {
                switch (subjectType) {
                    case CHOICES:
                        choicesHandler.judgeOptionRight(tempAnswer, subjectDto);
                        break;
                    case MULTIPLE_CHOICES:
                        multipleChoicesHandler.judgeOptionRight(tempAnswer, subjectDto);
                        break;
                    case SHORT_ANSWER:
                        shortAnswerHandler.judgeRight(tempAnswer, subjectDto);
                        break;
                    case JUDGEMENT:
                        judgementHandler.judgeRight(tempAnswer, subjectDto);
                        break;
                    default:
                        break;
                }
            }
            return answerDto;
        }).collect(Collectors.toList());
        Page<AnswerDto> pageInfo = new Page<>();
        pageInfo.setRecords(answerDtos);
        pageInfo.setTotal(page.getTotal());
        pageInfo.setCurrent(page.getCurrent());
        pageInfo.setSize(page.getSize());
        return pageInfo;
    }

    public ExecResult<AnswerDto> answerInfo(Long recordId, Optional<Long> currentSubjectId, Optional<Integer> nextSubjectType, Optional<Integer> nextType) {
        ExaminationRecord record = examRecordService.baseGetById(recordId);
        SubjectDto subjectDto;
        // 题目为空，则加载第一题
        if (currentSubjectId.isPresent()) {
            ExaminationSubject examinationSubject = new ExaminationSubject();
            examinationSubject.setExaminationId(record.getExaminationId());
            examinationSubject.setSubjectId(currentSubjectId.get());
            // 查询该考试和指定序号的题目的关联信息
            // 下一题
            if (AnswerConstant.NEXT.equals(nextType)) {
                examinationSubject = examinationSubjectService.findNext(record.getExaminationId(), currentSubjectId.get()).orElse(null);
            } else if (AnswerConstant.PREVIOUS.equals(nextType)) {
                // 上一题
                examinationSubject = examinationSubjectService.findPrevious(record.getExaminationId(), currentSubjectId.get()).orElse(null);
            } else {
                examinationSubject = examinationSubjectService.find(record.getExaminationId(), currentSubjectId.get()).orElse(null);
            }
            if (ParamsUtil.isEmpty(examinationSubject)) {
                throw new CommonException("ID为" + currentSubjectId.get() + "的题目不存在");
            }
            // 查询题目的详细信息
            subjectDto = subjectService.get(examinationSubject.getSubjectId(), examinationSubject.getType());
        } else {
            subjectDto = subjectService.findFirstSubjectByExaminationId(record.getExaminationId());
        }
        AnswerDto answerDto = new AnswerDto();
        answerDto.setSubject(subjectDto);
        // 查询答题
        Optional<Answer> userAnswerOpt = findAnswerByExamRecordIdAndSubjectId(recordId, subjectDto.getId());
        Answer answer = new Answer();
        answer.setSubjectId(subjectDto.getId());
        answer.setExamRecordId(recordId);
        if (userAnswerOpt.isPresent()) {
            answer = userAnswerOpt.get();
        }
        BeanUtils.copyProperties(answer, answerDto);
        answerDto.setDuration(ExamRecordUtil.getExamDuration(answer.getStartTime(), answer.getEndTime()));
        // 判断正误
        SubjectTypeEnum subjectType = SubjectTypeEnum.matchByValue(subjectDto.getType());
        if (subjectType != null) {
            switch (subjectType) {
                case CHOICES:
                    choicesHandler.judgeOptionRight(answer, subjectDto);
                    break;
                case MULTIPLE_CHOICES:
                    multipleChoicesHandler.judgeOptionRight(answer, subjectDto);
                    break;
                case SHORT_ANSWER:
                    shortAnswerHandler.judgeRight(answer, subjectDto);
                    break;
                case JUDGEMENT:
                    judgementHandler.judgeRight(answer, subjectDto);
                    break;
                default:
                    break;
            }
        }
        ExecResult<List<UserVo>> userVoResponseBean = userServiceClient.findUserById(Arrays.asList(record.getUserId()));
        if (userVoResponseBean.isSuccess() && ParamsUtil.isNotEmpty(userVoResponseBean.getData())) {
            UserVo userVo = userVoResponseBean.getData().get(0);
            answerDto.setUserName(userVo.getName());
        }
        return ExecResultUtil.success(answerDto);
    }

    public ExecResult<List<RankInfoDto>> getRankInfo(Long recordId) {
        List<RankInfoDto> rankInfos = new ArrayList<>();
        // 查询缓存
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(AnswerConstant.CACHE_PREFIX_RANK + recordId, 0, Integer.MAX_VALUE);
        if (ParamsUtil.isEmpty(typedTuples)) {
            return ExecResultUtil.success(rankInfos);
        }
        // 用户ID列表
        Set<Long> userIds = new HashSet<>();
        typedTuples.forEach(typedTuple -> {
            ExaminationRecord record = JsonMapper.getInstance()
                    .fromJson(typedTuple.getValue(), ExaminationRecord.class);
            if (ParamsUtil.isEmpty(record)) {
                return;
            }
            RankInfoDto rankInfo = new RankInfoDto();
            rankInfo.setUserId(record.getUserId());
            userIds.add(record.getUserId());
            rankInfo.setScore(typedTuple.getScore());
            rankInfos.add(rankInfo);
        });
        if (ParamsUtil.isEmpty(userIds)) {
            return ExecResultUtil.success(rankInfos);
        }
        ExecResult<List<UserVo>> userResponse = userServiceClient.findUserById(new ArrayList<>(userIds));
        if (userResponse.isError()) {
            return ExecResultUtil.success(rankInfos);
        }
        rankInfos.forEach(rankInfo -> {
            userResponse.getData().stream().filter(user -> user.getId().equals(rankInfo.getUserId()))
                    .findAny().ifPresent(user -> {
                // 设置考生信息
                rankInfo.setName(user.getName());
                rankInfo.setAvatarUrl(user.getAvatarUrl());
            });
        });
        return ExecResultUtil.success(rankInfos);
    }

    public ExecResult<Boolean> anonymousUserSubmit(Long examinationId, String identifier, List<SubjectDto> subjectDtos) {
        if (StringUtils.isBlank(identifier) || CollectionUtils.isEmpty(subjectDtos)) {
            return ExecResultUtil.success(false);
        }
        Examination examination = examinationService.baseGetById(examinationId);
        String tenantCode = SysUtil.getTenantCode();
        String sysCode = SysUtil.getSysCode();
        Date currentDate = DateUtils.asDate(LocalDateTime.now());
        // 判断用户是否存在，不存在则自动创建
        ExecResult<UserVo> userVoResponseBean = userServiceClient.findUserByIdentifier(identifier, tenantCode);
        if (userVoResponseBean.isError() || ParamsUtil.isEmpty(userVoResponseBean.getData())) {
            return ExecResultUtil.success(false);
        }
        // TODO 自动注册账号
        UserVo user = userVoResponseBean.getData();
        // 保存考试记录
        ExaminationRecord record = new ExaminationRecord();
        record.setCommonValue(identifier, sysCode, tenantCode);
        record.setUserId(user.getUserId());

        // 初始化Answer
        List<Answer> answers = new ArrayList<>(subjectDtos.size());
        subjectDtos.forEach(subjectDto -> {
            Answer answer = new Answer();
            answer.setCommonValue(identifier, sysCode, tenantCode);
            answer.setAnswer(subjectDto.getAnswer().getAnswer());
            answer.setExamRecordId(record.getId());
            answer.setEndTime(currentDate);
            answer.setSubjectId(subjectDto.getId());
            answer.setType(subjectDto.getType());
            answer.setAnswerType(AnswerConstant.WRONG);
            answers.add(answer);
        });
        // 分类题目
        Map<String, List<Answer>> distinctAnswer = this.distinctAnswer(answers);
        AnswerHandleResult result = handleAll(distinctAnswer);
        // 记录总分、正确题目数、错误题目数
        record.setScore(result.getScore());
        record.setCorrectNumber(result.getCorrectNum());
        record.setIncorrectNumber(result.getInCorrectNum());
        // 更新状态为统计完成，否则需要阅卷完成后才更改统计状态
        record.setExaminationId(examinationId);
        record.setSubmitStatus(SubmitStatusEnum.CALCULATED.getValue());
        record.setStartTime(currentDate);
        record.setEndTime(currentDate);
        examRecordService.baseSave(record);
        this.baseSave(answers);
        return ExecResultUtil.success(true);

    }

    /**
     * 分类答题
     *
     * @param answers answers
     * @return Map
     * @author fanzh
     * @date 2019/06/18 16:32
     */
    private Map<String, List<Answer>> distinctAnswer(List<Answer> answers) {
        Map<String, List<Answer>> distinctMap = new HashMap<>();
        answers.stream().collect(Collectors.groupingBy(Answer::getType, Collectors.toList())).forEach((type, temp) -> {
            // 匹配类型
            SubjectTypeEnum subjectType = SubjectTypeEnum.matchByValue(type);
            if (subjectType != null) {
                switch (subjectType) {
                    case CHOICES:
                        distinctMap.put(SubjectTypeEnum.CHOICES.name(), temp);
                        break;
                    case MULTIPLE_CHOICES:
                        distinctMap.put(SubjectTypeEnum.MULTIPLE_CHOICES.name(), temp);
                        break;
                    case SHORT_ANSWER:
                        distinctMap.put(SubjectTypeEnum.SHORT_ANSWER.name(), temp);
                        break;
                    case JUDGEMENT:
                        distinctMap.put(SubjectTypeEnum.JUDGEMENT.name(), temp);
                        break;
                    default:
                        break;
                }
            }
        });
        return distinctMap;
    }

    /**
     * 自动判分
     *
     * @param distinctAnswer distinctAnswer
     * @return ResponseBean
     * @author fanzh
     * @date 2020/03/15 16:21
     */
    public AnswerHandleResult handleAll(Map<String, List<Answer>> distinctAnswer) {
        // 暂时只自动统计单选题、多选题、判断题，简答题由老师阅卷批改
        AnswerHandleResult choiceResult = choicesHandler.handle(distinctAnswer.get(SubjectTypeEnum.CHOICES.name()));
        AnswerHandleResult multipleResult = multipleChoicesHandler.handle(distinctAnswer.get(SubjectTypeEnum.MULTIPLE_CHOICES.name()));
        AnswerHandleResult judgementResult = judgementHandler.handle(distinctAnswer.get(SubjectTypeEnum.JUDGEMENT.name()));
        AnswerHandleResult shortAnswerResult = shortAnswerHandler.handle(distinctAnswer.get(SubjectTypeEnum.SHORT_ANSWER.name()));
        return AnswerHandlerUtil.addAll(Arrays.asList(choiceResult, multipleResult, judgementResult, shortAnswerResult));
    }

    /**
     * 开始考试
     *
     * @param userId
     * @param identifier
     * @param examinationId
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public StartExamDto start(
            Long userId
            , String identifier
            , Long examinationId
    ) {
        if (ParamsUtil.isEmpty(userId)) {
            throw new CommonException("Method answerService.start(), Param userId cannot be empty");
        }
        if (ParamsUtil.isEmpty(identifier)) {
            throw new CommonException("Method answerService.start(), Param identifier cannot be empty");
        }
        if (ParamsUtil.isEmpty(examinationId)) {
            throw new CommonException("Method answerService.start(), Param examinationId cannot be empty");
        }

        ExaminationRecord examRecord = new ExaminationRecord();
        examRecord.setCreator(identifier);
        examRecord.setModifier(identifier);
        examRecord.setUserId(userId);
        examRecord.setExaminationId(examinationId);
        examRecord.setStartTime(examRecord.getCreateDate());
        // 默认未提交状态
        examRecord.setSubmitStatus(SubmitStatusEnum.NOT_SUBMITTED.getValue());
        examRecordService.baseSave(examRecord);
        // 查找考试信息
        Examination examination = examinationService.baseGetById(examinationId);
        StartExamDto startExamDto = new StartExamDto();
        // 保存考试记录
        startExamDto.setExamination(examination);
        startExamDto.setExamRecord(examRecord);
        // 根据题目ID，类型获取第一题的详细信息
        SubjectDto subjectDto = subjectService.findFirstSubjectByExaminationId(examRecord.getExaminationId());
        startExamDto.setSubjectDto(subjectDto);
        // 创建第一题的答题
        Answer answer = new Answer();
        answer.setCreator(identifier);
        answer.setModifier(identifier);
        answer.setExamRecordId(examRecord.getId());
        answer.setSubjectId(subjectDto.getId());
        // 默认待批改状态
        answer.setMarkStatus(AnswerConstant.TO_BE_MARKED);
        answer.setAnswerType(AnswerConstant.WRONG);
        answer.setStartTime(answer.getCreateDate());
        // 保存答题
        this.baseSave(answer);
        subjectDto.setAnswer(answer);
        return startExamDto;
    }


    /**
     * 开始考试
     *
     * @param identifier
     * @param examinationId
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public StartExamDto anonymousUserStart(String identifier, Long examinationId) {
        if (ParamsUtil.isEmpty(identifier)) {
            throw new CommonException("Method answerService.anonymousUserStart(), Param identifier cannot be empty");
        }
        if (ParamsUtil.isEmpty(examinationId)) {
            throw new CommonException("Method answerService.anonymousUserStart(), Param examinationId cannot be empty");
        }
        ExecResult<UserVo> userRet = userServiceClient.findUserByIdentifier(identifier, SysUtil.getTenantCode());
        if (userRet.isError()) {
            throw new CommonException("获取用户" + identifier + "信息失败！");
        }
        return this.start(userRet.getData().getUserId(), identifier, examinationId);
    }

    /**
     * 完成批改
     *
     * @param examRecordId
     */
    public void completeMarking(Long examRecordId) {
        ExaminationRecord examRecord = examRecordService.baseGetById(examRecordId);
        List<Answer> answers = this.findAnswerByExamRecordId(examRecord.getId());
        if (ParamsUtil.isEmpty(answers)) {
            return;
        }
        long correctNumber = answers.stream().filter(tempAnswer -> tempAnswer.getAnswerType().equals(AnswerConstant.RIGHT)).count();
        // 总分
        BigDecimal score = answers.stream().map(Answer::getScore).reduce(BigDecimal.ZERO, BigDecimal::add);
        examRecord.setScore(score);
        examRecord.setSubmitStatus(SubmitStatusEnum.CALCULATED.getValue());
        examRecord.setCorrectNumber((int) correctNumber);
        examRecord.setIncorrectNumber(answers.size() - examRecord.getCorrectNumber());
        examRecordService.baseSave(examRecord);
    }

    public List<Answer> findAnswerByExamRecordId(Long examRecordId) {
        if (ParamsUtil.isEmpty(examRecordId)) {
            return Collections.emptyList();
        }
        EntityWrapper<Answer> ew = EntityWrapperUtil.build();
        ew.eq("exam_record_id", examRecordId);
        return selectList(ew);
    }

    /**
     * 提交答卷，自动统计选择题得分
     *
     * @param answer answer
     * @author fanzh
     * @date 2018/12/26 14:09
     */
    @Transactional(rollbackFor = Throwable.class)
    public void submit(Answer answer) {
        String currentUsername = answer.getModifier();
        // 查找已提交的题目
        List<Answer> answerList = this.baseList(answer);
        if (CollectionUtils.isEmpty(answerList)) {
            return;
        }
        // 成绩
        ExaminationRecord record = new ExaminationRecord();
        // 分类题目
        Map<String, List<Answer>> distinctAnswer = this.distinctAnswer(answerList);
        AnswerHandleResult result = handleAll(distinctAnswer);
        // 记录总分、正确题目数、错误题目数
        record.setScore(result.getScore());
        record.setCorrectNumber(result.getCorrectNum());
        record.setIncorrectNumber(result.getInCorrectNum());
        // 更新答题状态
        distinctAnswer.values().forEach(answers -> answers.forEach(this::updateAnswer));
        // 更新状态为统计完成，否则需要阅卷完成后才更改统计状态
        record.setSubmitStatus(SubmitStatusEnum.CALCULATED.getValue());
        // 保存成绩
        record.setCommonValue(currentUsername, SysUtil.getSysCode(), SysUtil.getTenantCode());
        record.setId(answer.getExamRecordId());
        record.setEndTime(record.getCreateDate());
        examRecordService.baseSave(record);
        // 更新排名数据
        updateRank(record);
    }

    /**
     * 更新排名信息
     * 基于Redis的sort set数据结构
     *
     * @param record record
     * @author fanzh
     * @date 2019/12/8 23:21
     */
    private void updateRank(ExaminationRecord record) {
        redisTemplate.opsForZSet().add(AnswerConstant.CACHE_PREFIX_RANK + record.getExaminationId(), JsonMapper.getInstance().toJson(record), record.getScore().doubleValue());
    }
}
