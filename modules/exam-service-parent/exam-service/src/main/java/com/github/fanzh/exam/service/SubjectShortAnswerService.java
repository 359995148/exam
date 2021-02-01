package com.github.fanzh.exam.service;

import com.github.fanzh.exam.mapper.SubjectShortAnswerMapper;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.exam.api.dto.SubjectDto;
import com.github.fanzh.exam.api.module.SubjectShortAnswer;
import com.github.fanzh.exam.utils.SubjectUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 简答题service
 *
 * @author fanzh
 * @date 2019/6/16 14:58
 */
@Service
public class SubjectShortAnswerService extends BaseService<SubjectShortAnswerMapper, SubjectShortAnswer> implements ISubjectService {

    @Override
    public SubjectDto findSubject(Long id) {
        return SubjectUtil.subjectShortAnswerToDto(this.baseFindById(id).orElse(null), true);
    }


    @Override
    public List<SubjectDto> findSubject(Set<Long> id) {
        List<SubjectShortAnswer> list = this.baseFindById(id);
        if (ParamsUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return SubjectUtil.subjectShortAnswerToDto(list, true);
    }

    @Override
    public SubjectDto getNextByCurrentIdAndType(Long examinationId, Long previousId, Integer nextType) {
        return null;
    }

    /**
     * 更新
     *
     * @param subjectDto subjectDto
     * @return SubjectDto
     * @author fanzh
     * @date 2019/06/16 17:54
     */
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean updateSubject(SubjectDto subjectDto) {
        Optional<SubjectShortAnswer> opt = this.baseFindById(subjectDto.getId());
        if (!opt.isPresent()) {
            return false;
        }
        SubjectShortAnswer subjectShortAnswer = new SubjectShortAnswer();
        BeanUtils.copyProperties(subjectDto, subjectShortAnswer);
        // 参考答案
        subjectShortAnswer.setAnswer(subjectDto.getAnswer().getAnswer());
        this.baseSave(subjectShortAnswer);
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
