package com.github.fanzh.exam.service;

import com.github.fanzh.exam.enums.SubjectTypeEnum;
import com.github.fanzh.common.basic.id.IdGen;
import com.github.fanzh.common.core.exceptions.CommonException;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.common.core.utils.SpringContextHolder;
import com.github.fanzh.exam.api.dto.SubjectDto;
import com.github.fanzh.exam.api.module.ExaminationSubject;
import com.github.fanzh.exam.api.module.SubjectChoices;
import com.github.fanzh.exam.api.module.SubjectJudgement;
import com.github.fanzh.exam.api.module.SubjectOption;
import com.github.fanzh.exam.api.module.SubjectShortAnswer;
import com.github.fanzh.exam.utils.SubjectUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目service
 *
 * @author fanzh
 * @date 2019/6/16 17:22
 */
@AllArgsConstructor
@Slf4j
@Service
public class SubjectService {

    private final SubjectChoicesService subjectChoicesService;
    private final SubjectShortAnswerService subjectShortAnswerService;
    private final ExaminationSubjectService examinationSubjectService;
    private final SubjectJudgementService subjectJudgementService;
    private final SubjectOptionService subjectOptionService;

    /**
     * 根据题目ID，题目类型查询题目信息
     *
     * @param id   id
     * @param type type
     * @return SubjectDto
     * @author fanzh
     * @date 2019/06/16 17:24
     */
    public SubjectDto get(Long id, Integer type) {
        return subjectService(type).findSubject(id);
    }

    /**
     * 根据题目ID查询题目信息
     *
     * @param id id
     * @return SubjectDto
     * @author fanzh
     * @date 2019/06/16 17:26
     */
    public SubjectDto get(Long id) {
        Integer type = SubjectTypeEnum.CHOICES.getValue();
        List<ExaminationSubject> examinationSubjects = examinationSubjectService.findBySubjectId(id);
        if (CollectionUtils.isNotEmpty(examinationSubjects)) {
            type = examinationSubjects.get(0).getType();
        }
        return subjectService(type).findSubject(id);
    }

    /**
     * 根据上一题ID查找
     *
     * @param examinationId examinationId
     * @param subjectId     subjectId
     * @param type          type
     * @param nextType      0：下一题，1：上一题
     * @return SubjectDto
     * @author fanzh
     * @date 2019/06/18 13:49
     */
    @Transactional
    public SubjectDto getNextByCurrentIdAndType(Long examinationId, Long subjectId, Integer type, Integer nextType) {
        return subjectService(type).getNextByCurrentIdAndType(examinationId, subjectId, nextType);
    }

    /**
     * 根据题目类型返回对应的BaseSubjectService
     *
     * @param type type
     * @return BaseSubjectService
     * @author fanzh
     * @date 2019/06/16 17:34
     */
    public ISubjectService subjectService(Integer type) {
        return SpringContextHolder.getApplicationContext().getBean(SubjectTypeEnum.matchByValue(type).getService());
    }

    /**
     * 新增题目
     *
     * @param subjects subjects
     * @return int
     * @author fanzh
     * @date 2019/06/17 14:39
     */
    @Transactional(rollbackFor = Throwable.class)
    public void addSubject(List<SubjectDto> subjects) {
        if (ParamsUtil.isEmpty(subjects)) {
            return;
        }
        Map<Integer, List<SubjectDto>> subjectGroup = subjects.stream().collect(Collectors.groupingBy(o -> o.getType()));
        for (Map.Entry<Integer, List<SubjectDto>> entry : subjectGroup.entrySet()) {
            Optional<SubjectTypeEnum> subjectType = SubjectTypeEnum.match(entry.getKey());
            if (!subjectType.isPresent()) {
                continue;
            }
            List<ExaminationSubject> examinationSubjectList = new ArrayList<>();
            entry.getValue().forEach(temp -> {
                if (ParamsUtil.isEmpty(temp.getId())) {
                    temp.setId(IdGen.snowflakeId());
                    ExaminationSubject examinationSubject = new ExaminationSubject();
                    examinationSubject.setExaminationId(temp.getExaminationId());
                    examinationSubject.setCategoryId(temp.getCategoryId());
                    examinationSubject.setSubjectId(temp.getId());
                    examinationSubject.setType(temp.getType());
                    examinationSubjectList.add(examinationSubject);
                }
            });
            examinationSubjectService.baseSave(examinationSubjectList);
            switch (subjectType.get()) {
                case CHOICES:
                    List<SubjectChoices> scList = new ArrayList<>();
                    entry.getValue().forEach(temp -> {
                        SubjectChoices subjectChoices = new SubjectChoices();
                        BeanUtils.copyProperties(temp, subjectChoices);
                        subjectChoices.setAnswer(temp.getAnswer().getAnswer());
                        subjectChoices.setChoicesType(temp.getType());
                        subjectChoicesService.insertOptions(subjectChoices);
                        scList.add(subjectChoices);
                    });
                    subjectChoicesService.baseSave(scList);
                    break;
                case MULTIPLE_CHOICES:
                    List<SubjectChoices> mscList = new ArrayList<>();
                    entry.getValue().forEach(temp -> {
                        SubjectChoices subjectChoices = new SubjectChoices();
                        BeanUtils.copyProperties(temp, subjectChoices);
                        subjectChoices.setAnswer(temp.getAnswer().getAnswer());
                        subjectChoices.setChoicesType(temp.getType());
                        subjectChoicesService.insertOptions(subjectChoices);
                        mscList.add(subjectChoices);
                    });
                    subjectChoicesService.baseSave(mscList);
                    break;
                case JUDGEMENT:
                    List<SubjectJudgement> sjList = new ArrayList<>();
                    entry.getValue().forEach(temp -> {
                        SubjectJudgement sj = new SubjectJudgement();
                        BeanUtils.copyProperties(temp, sj);
                        sj.setAnswer(temp.getAnswer().getAnswer());
                        sjList.add(sj);
                    });
                    subjectJudgementService.baseSave(sjList);
                    break;
                case SHORT_ANSWER:
                    List<SubjectShortAnswer> saList = new ArrayList<>();
                    entry.getValue().forEach(temp -> {
                        SubjectShortAnswer sa = new SubjectShortAnswer();
                        BeanUtils.copyProperties(temp, sa);
                        sa.setAnswer(temp.getAnswer().getAnswer());
                        saList.add(sa);
                    });
                    subjectShortAnswerService.baseSave(saList);
                    break;
                default:
            }
        }
    }

    public void updateSubject(SubjectDto subject) {
        boolean flag = subjectService(subject.getType()).updateSubject(subject);
        if (flag) {
            return;
        }
        addSubject(Arrays.asList(subject));
    }


    /**
     * 遍历关系集合，按类型分组题目ID，返回map
     *
     * @param examinationSubjects examinationSubjects
     * @return Map
     * @author fanzh
     * @date 2019/06/17 10:43
     */
    private Map<Integer, Set<Long>> getSubjectIdByType(List<ExaminationSubject> examinationSubjects) {
        Map<Integer, Set<Long>> idMap = new HashMap<>();
        examinationSubjects.stream().collect(Collectors.groupingBy(ExaminationSubject::getType, Collectors.toList()))
                .forEach((type, temp) -> {
                    // 匹配类型
                    SubjectTypeEnum subjectType = SubjectTypeEnum.matchByValue(type);
                    if (subjectType != null) {
                        switch (subjectType) {
                            case CHOICES:
                                idMap.put(SubjectTypeEnum.CHOICES.getValue(),
                                        temp.stream().map(ExaminationSubject::getSubjectId)
                                                .collect(Collectors.toSet()));
                                break;
                            case JUDGEMENT:
                                idMap.put(SubjectTypeEnum.JUDGEMENT.getValue(),
                                        temp.stream().map(ExaminationSubject::getSubjectId)
                                                .collect(Collectors.toSet()));
                                break;
                            case MULTIPLE_CHOICES:
                                idMap.put(SubjectTypeEnum.MULTIPLE_CHOICES.getValue(),
                                        temp.stream().map(ExaminationSubject::getSubjectId)
                                                .collect(Collectors.toSet()));
                                break;
                            case SHORT_ANSWER:
                                idMap.put(SubjectTypeEnum.SHORT_ANSWER.getValue(),
                                        temp.stream().map(ExaminationSubject::getSubjectId)
                                                .collect(Collectors.toSet()));
                                break;
                        }
                    }
                });
        return idMap;
    }

    /**
     * 根据关系列表查询对应的题目的详细信息
     *
     * @param examinationSubjects examinationSubjects
     * @return List
     * @author fanzh
     * @date 2019/06/17 10:54
     */
    public List<SubjectDto> findSubjectDtoList(List<ExaminationSubject> examinationSubjects) {
        return findSubjectDtoList(examinationSubjects, false);
    }

    /**
     * 根据关系列表查询对应的题目的详细信息
     *
     * @param examinationSubjects examinationSubjects
     * @param findOptions         findOptions
     * @return List
     * @author fanzh
     * @date 2019/06/17 11:54
     */
    public List<SubjectDto> findSubjectDtoList(List<ExaminationSubject> examinationSubjects, boolean findOptions) {
        return findSubjectDtoList(examinationSubjects, findOptions, true);
    }

    /**
     * 根据关系列表查询对应的题目的详细信息
     *
     * @param examinationSubjects examinationSubjects
     * @param findOptions         findOptions
     * @param findAnswer          findAnswer
     * @return List
     * @author fanzh
     * @date 2019/06/17 11:54
     */
    public List<SubjectDto> findSubjectDtoList(List<ExaminationSubject> examinationSubjects, boolean findOptions, boolean findAnswer) {
        Map<Integer, Set<Long>> subjectIdMap = this.getSubjectIdByType(examinationSubjects);
        // 查询题目信息，聚合
        List<SubjectDto> subjectDtoList = new ArrayList<>();
        if (subjectIdMap.containsKey(SubjectTypeEnum.CHOICES.getValue())) {
            List<SubjectChoices> subjectChoicesList = subjectChoicesService.baseFindById(subjectIdMap.get(SubjectTypeEnum.CHOICES.getValue()));
            if (CollectionUtils.isNotEmpty(subjectChoicesList)) {
                // 查找选项信息
                if (findOptions) {
                    List<SubjectOption> subjectOptionList = subjectOptionService.findBySubjectChoicesId(subjectChoicesList.stream().map(o -> o.getId()).collect(Collectors.toSet()));
                    subjectChoicesList.forEach(tempSubjectChoices -> {
                        tempSubjectChoices.setOptions(subjectOptionList.stream().filter(tempOption -> Objects.equals(tempOption.getSubjectChoicesId(), tempSubjectChoices.getId())).collect(Collectors.toList()));
                    });
                }
                subjectDtoList.addAll(SubjectUtil.subjectChoicesToDto(subjectChoicesList, findAnswer));
            }
        }

        if (subjectIdMap.containsKey(SubjectTypeEnum.MULTIPLE_CHOICES.getValue())) {
            List<SubjectChoices> subjectChoicesList = subjectChoicesService.baseFindById(subjectIdMap.get(SubjectTypeEnum.MULTIPLE_CHOICES.getValue()));
            if (CollectionUtils.isNotEmpty(subjectChoicesList)) {
                // 查找选项信息
                if (findOptions) {
                    List<SubjectOption> subjectOptionList = subjectOptionService.findBySubjectChoicesId(subjectChoicesList.stream().map(o -> o.getId()).collect(Collectors.toSet()));
                    subjectChoicesList.forEach(tempSubjectChoices -> {
                        tempSubjectChoices.setOptions(subjectOptionList.stream().filter(tempOption -> Objects.equals(tempOption.getSubjectChoicesId(), tempSubjectChoices.getId())).collect(Collectors.toList()));
                    });
                }
                subjectDtoList.addAll(SubjectUtil.subjectChoicesToDto(subjectChoicesList, findAnswer));
            }
        }
        if (subjectIdMap.containsKey(SubjectTypeEnum.SHORT_ANSWER.getValue())) {
            List<SubjectShortAnswer> subjectShortAnswers = subjectShortAnswerService.baseFindById(subjectIdMap.get(SubjectTypeEnum.SHORT_ANSWER.getValue()));
            if (CollectionUtils.isNotEmpty(subjectShortAnswers)) {
                subjectDtoList.addAll(SubjectUtil.subjectShortAnswerToDto(subjectShortAnswers, findAnswer));
            }
        }
        if (subjectIdMap.containsKey((SubjectTypeEnum.JUDGEMENT.getValue()))) {
            List<SubjectJudgement> subjectJudgements = subjectJudgementService.baseFindById(subjectIdMap.get(SubjectTypeEnum.JUDGEMENT.getValue()));
            if (CollectionUtils.isNotEmpty(subjectJudgements)) {
                subjectDtoList.addAll(SubjectUtil.subjectJudgementsToDto(subjectJudgements, findAnswer));
            }
        }
        return subjectDtoList;
    }

    /**
     * 查询第一题
     *
     * @param examinationId examinationId
     * @return SubjectDto
     * @author fanzh
     * @date 2019/10/13 18:36:58
     */
    public SubjectDto findFirstSubjectByExaminationId(Long examinationId) {
        if (ParamsUtil.isEmpty(examinationId)) {
            throw new CommonException("Method subjectService.findFirstSubjectByExaminationId(), Param examinationId cannot be empty");
        }
        // 根据考试ID查询考试题目管理关系，题目ID递增
        Optional<ExaminationSubject> examinationSubjectOpt = examinationSubjectService.findFirstByExaminationId(examinationId);
        if (!examinationSubjectOpt.isPresent()) {
            throw new CommonException("ExaminationSubject does empty by examinationId: " + examinationId);
        }
        // 第一题
        ExaminationSubject examinationSubject = examinationSubjectOpt.get();
        // 根据题目ID，类型获取题目的详细信息
        return this.get(examinationSubject.getSubjectId(), examinationSubject.getType());
    }

    /**
     * 导出
     *
     * @param ids           ids
     * @param examinationId examinationId
     * @param categoryId    categoryId
     * @return List
     */
    public List<SubjectDto> exportSubject(List<Long> ids, Long examinationId, Long categoryId) {
        List<SubjectDto> subjects = new ArrayList<>();
        List<ExaminationSubject> examinationSubjects = new ArrayList<>();
        // 根据题目id导出
        if (ParamsUtil.isNotEmpty(ids)) {
            examinationSubjects.addAll(examinationSubjectService.findBySubjectId(SetUtil.build(ids)));
        } else if (ParamsUtil.isNotEmpty(examinationId)) {
            // 根据考试ID
            examinationSubjects = examinationSubjectService.findByExaminationId(examinationId);
        } else if (ParamsUtil.isNotEmpty(categoryId)) {
            // 根据分类ID、类型导出
            examinationSubjects = examinationSubjectService.findByCategoryId(categoryId);
        }
        if (CollectionUtils.isNotEmpty(examinationSubjects)) {
            for (ExaminationSubject es : examinationSubjects) {
                SubjectDto subjectDto = this.get(es.getSubjectId(), es.getType());
                subjectDto.setExaminationId(es.getExaminationId());
                subjectDto.setCategoryId(es.getCategoryId());
                subjects.add(subjectDto);
            }
        }
        return subjects;
    }

    /**
     * 删除
     *
     * @param id
     */
    @Transactional(rollbackFor = Throwable.class)
    public void deleteSubject(Set<Long> id) {
        if (ParamsUtil.isEmpty(id)) {
            return;
        }
        List<ExaminationSubject> examinationSubjectList = examinationSubjectService.findBySubjectId(id);
        Map<Integer, List<ExaminationSubject>> group = examinationSubjectList.stream().collect(Collectors.groupingBy(o -> o.getType()));
        for (Map.Entry<Integer, List<ExaminationSubject>> entry : group.entrySet()) {
            ISubjectService service = subjectService(entry.getKey());
            if (ParamsUtil.isEmpty(service)) {
                continue;
            }
            service.deleteSubject(id);
        }
        examinationSubjectService.baseDelete(examinationSubjectList.stream().map(o -> o.getId()).collect(Collectors.toSet()));
    }
}
