package com.github.fanzh.exam.service;

import com.github.fanzh.exam.api.dto.SubjectDto;

import java.util.List;
import java.util.Set;

/**
 * 题目通用接口
 *
 * @author fanzh
 * @date 2019/6/16 17:30
 */
public interface ISubjectService {

    /**
     * 查询
     *
     * @param id
     * @return
     */
    SubjectDto findSubject(Long id);

    /**
     * 查询
     *
     * @param id
     * @return
     */
    List<SubjectDto> findSubject(Set<Long> id);

    /**
     * 根据ID查询上一题、下一题
     *
     * @param examinationId examinationId
     * @param previousId    previousId
     * @param nextType      -1：当前题目，0：下一题，1：上一题
     * @return SubjectDto
     * @author fanzh
     * @date 2019-09-14 16:33
     */
    SubjectDto getNextByCurrentIdAndType(Long examinationId, Long previousId, Integer nextType);

    /**
     * 更新
     *
     * @param subject
     * @return
     */
    boolean updateSubject(SubjectDto subject);

    /**
     * 删除
     *
     * @param id
     */
    void deleteSubject(Set<Long> id);
}
