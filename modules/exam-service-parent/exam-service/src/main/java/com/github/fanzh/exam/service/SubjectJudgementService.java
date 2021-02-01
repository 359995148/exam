package com.github.fanzh.exam.service;

import com.github.fanzh.exam.mapper.SubjectJudgementMapper;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.exam.api.dto.SubjectDto;
import com.github.fanzh.exam.api.module.SubjectJudgement;
import com.github.fanzh.exam.utils.SubjectUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 判断题Service
 *
 * @author fanzh
 * @date 2019-07-16 13:02
 */
@AllArgsConstructor
@Slf4j
@Service
public class SubjectJudgementService extends BaseService<SubjectJudgementMapper, SubjectJudgement> implements ISubjectService {

    /**
     * 根据ID查询
     *
     * @param id id
     * @return SubjectDto
     * @author fanzh
     * @date 2019-07-16 13:06
     */
    @Override
    public SubjectDto findSubject(Long id) {
        return SubjectUtil.subjectJudgementToDto(this.baseFindById(id).orElse(null));
    }

    @Override
    public List<SubjectDto> findSubject(Set<Long> id) {
        List<SubjectJudgement> list = this.baseFindById(id);
        if (ParamsUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return SubjectUtil.subjectJudgementsToDto(list, true);
    }

    /**
     * 根据上一题ID查询下一题
     *
     * @param examinationId examinationId
     * @param previousId    previousId
     * @param nextType      0：下一题，1：上一题
     * @return SubjectDto
     * @author fanzh
     * @date 2019-09-14 17:03
     */
    @Override
    public SubjectDto getNextByCurrentIdAndType(Long examinationId, Long previousId, Integer nextType) {
        return null;
    }

    /**
     * 更新
     *
     * @param subjectDto subjectDto
     * @return int
     * @author fanzh
     * @date 2019-07-16 13:10
     */
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean updateSubject(SubjectDto subjectDto) {
        Optional<SubjectJudgement> opt = this.baseFindById(subjectDto.getId());
        if (!opt.isPresent()) {
            return false;
        }
        SubjectJudgement subjectJudgement = new SubjectJudgement();
        BeanUtils.copyProperties(subjectDto, subjectJudgement);
        subjectJudgement.setAnswer(subjectDto.getAnswer().getAnswer());
        this.baseSave(subjectJudgement);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void deleteSubject(Set<Long> id) {
        if (ParamsUtil.isEmpty(id)) {
            return ;
        }
        this.baseDelete(id);
    }
}
