package com.github.fanzh.exam.service;

import com.github.fanzh.exam.mapper.SubjectChoicesMapper;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.exam.api.constants.AnswerConstant;
import com.github.fanzh.exam.api.dto.SubjectDto;
import com.github.fanzh.exam.api.module.ExaminationSubject;
import com.github.fanzh.exam.api.module.SubjectChoices;
import com.github.fanzh.exam.api.module.SubjectOption;
import com.github.fanzh.exam.utils.AnswerHandlerUtil;
import com.github.fanzh.exam.utils.SubjectUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 选择题service
 *
 * @author fanzh
 * @date 2018/11/8 21:23
 */
@AllArgsConstructor
@Service
public class SubjectChoicesService extends BaseService<SubjectChoicesMapper, SubjectChoices> implements ISubjectService {

    private final SubjectOptionService subjectOptionService;

    private final ExaminationSubjectService examinationSubjectService;

    /**
     * 根据ID查询
     *
     * @param id id
     * @return SubjectDto
     * @author fanzh
     * @date 2019/06/16 17:36
     */
    @Override
    public SubjectDto findSubject(Long id) {
        Optional<SubjectChoices> opt = this.baseFindById(id);
        if (!opt.isPresent()) {
            return null;
        }
        // 查找选项信息
        SubjectChoices subject = opt.get();
        List<SubjectOption> options = subjectOptionService.findBySubjectChoicesId(subject.getId());
        subject.setOptions(options);
        return SubjectUtil.subjectChoicesToDto(subject, true);
    }

    @Override
    public List<SubjectDto> findSubject(Set<Long> id) {
        List<SubjectChoices> list = this.baseFindById(id);
        if (ParamsUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 查找选项信息
        List<SubjectOption> optionList = subjectOptionService.findBySubjectChoicesId(list.stream().map(o -> o.getId()).collect(Collectors.toSet()));
        list.forEach(tempSubject -> {
            tempSubject.setOptions(optionList.stream().filter(o -> Objects.equals(o.getSubjectChoicesId(), tempSubject.getId())).collect(Collectors.toList()));
        });
        return SubjectUtil.subjectChoicesToDto(list, true);
    }

    /**
     * 根据上一题ID查询下一题
     *
     * @param examinationId examinationId
     * @param subjectId     subjectId
     * @param nextType      0：下一题，1：上一题
     * @return SubjectDto
     * @author fanzh
     * @date 2019/09/14 16:35
     */
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public SubjectDto getNextByCurrentIdAndType(Long examinationId, Long subjectId, Integer nextType) {
        Long id;
        if (AnswerConstant.CURRENT.equals(nextType)) {
            id = subjectId;
        } else if (AnswerConstant.NEXT.equals(nextType)) {
            Optional<ExaminationSubject> opt = examinationSubjectService.findNext(examinationId, subjectId);
            if (!opt.isPresent()) {
                return null;
            }
            id = opt.get().getSubjectId();
        } else {
            Optional<ExaminationSubject> opt = examinationSubjectService.findPrevious(examinationId, subjectId);
            if (!opt.isPresent()) {
                return null;
            }
            id = opt.get().getSubjectId();
        }
        SubjectChoices subjectChoices = this.baseGetById(id);
        List<SubjectOption> options = subjectOptionService.findBySubjectChoicesId(id);
        subjectChoices.setOptions(options);
        return SubjectUtil.subjectChoicesToDto(subjectChoices, true);
    }

    /**
     * 更新，包括更新选项
     *
     * @param subjectDto subjectDto
     * @return int
     * @author fanzh
     * @date 2019/06/16 17:50
     */
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean updateSubject(SubjectDto subjectDto) {
        Optional<SubjectChoices> opt = this.baseFindById(subjectDto.getId());
        if (!opt.isPresent()) {
            return false;
        }
        SubjectChoices subjectChoices = new SubjectChoices();
        BeanUtils.copyProperties(subjectDto, subjectChoices);
        subjectChoices.setCommonValue(SysUtil.getUser(), SysUtil.getSysCode(), SysUtil.getTenantCode());
        // 参考答案
        subjectChoices.setAnswer(AnswerHandlerUtil.replaceComma(subjectDto.getAnswer().getAnswer()));
        this.baseSave(subjectChoices);
        return true;
    }

    /**
     * 保存选项
     *
     * @param subjectChoices subjectChoices
     * @author fanzh
     * @date 2020/01/17 22:30:48
     */
    @Transactional(rollbackFor = Throwable.class)
    public void insertOptions(SubjectChoices subjectChoices) {
        if (ParamsUtil.isEmpty(subjectChoices.getOptions())) {
            return;
        }
        subjectOptionService.deleteBySubjectChoicesId(SetUtil.build(subjectChoices.getId()));
        // 初始化
        subjectChoices.getOptions().forEach(option -> {
            option.setCommonValue(
                    subjectChoices.getCreator()
                    , subjectChoices.getApplicationCode()
                    , subjectChoices.getTenantCode()
            );
            option.setSubjectChoicesId(subjectChoices.getId());
        });
        // 批量插入
        subjectOptionService.baseSave(subjectChoices.getOptions());
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void deleteSubject(Set<Long> id) {
        if (ParamsUtil.isEmpty(id)) {
            return;
        }
        this.baseDelete(id);
        // 删除选项
        subjectOptionService.deleteBySubjectChoicesId(id);
    }

}
