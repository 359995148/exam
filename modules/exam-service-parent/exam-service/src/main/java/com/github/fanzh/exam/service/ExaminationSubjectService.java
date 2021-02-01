package com.github.fanzh.exam.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.exam.mapper.ExaminationSubjectMapper;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.basic.utils.EntityWrapperUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.exam.api.module.ExaminationSubject;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 考试题目关联service
 *
 * @author fanzh
 * @date 2019/6/16 15:38
 */
@AllArgsConstructor
@Service
public class ExaminationSubjectService extends BaseService<ExaminationSubjectMapper, ExaminationSubject> {


    /**
     * 根据考试id查询题目id列表
     *
     * @param examinationId examinationId
     * @return int
     * @author fanzh
     * @date 2019/06/18 14:35
     */
    public List<ExaminationSubject> findByExaminationId(Long examinationId) {
        if (ParamsUtil.isEmpty(examinationId)) {
            return Collections.emptyList();
        }
        return findByExaminationId(SetUtil.build(examinationId));
    }

    /**
     * 根据考试id查询题目id列表
     *
     * @param examinationId examinationId
     * @return int
     * @author fanzh
     * @date 2019/06/18 14:35
     */
    public List<ExaminationSubject> findByExaminationId(Set<Long> examinationId) {
        if (ParamsUtil.isEmpty(examinationId)) {
            return Collections.emptyList();
        }
        EntityWrapper<ExaminationSubject> ew = EntityWrapperUtil.build();
        ew.orderAsc(Arrays.asList("subject_id"));
        ew.in("examination_id", examinationId);
        return selectList(ew);
    }

    public List<ExaminationSubject> findBySubjectId(Long subjectId) {
        if (ParamsUtil.isEmpty(subjectId)) {
            return Collections.emptyList();
        }
        return findBySubjectId(SetUtil.build(subjectId));
    }

    public List<ExaminationSubject> findBySubjectId(Set<Long> subjectId) {
        if (ParamsUtil.isEmpty(subjectId)) {
            return Collections.emptyList();
        }
        EntityWrapper<ExaminationSubject> ew = EntityWrapperUtil.build();
        ew.in("subject_id", subjectId);
        return selectList(ew);
    }

    public List<ExaminationSubject> findByCategoryId(Long categoryId) {
        if (ParamsUtil.isEmpty(categoryId)) {
            return Collections.emptyList();
        }
        return findBySubjectId(SetUtil.build(categoryId));
    }

    public List<ExaminationSubject> findByCategoryId(Set<Long> categoryId) {
        if (ParamsUtil.isEmpty(categoryId)) {
            return Collections.emptyList();
        }
        EntityWrapper<ExaminationSubject> ew = EntityWrapperUtil.build();
        ew.in("category_id", categoryId);
        return selectList(ew);
    }

    /**
     * 根据考试id查询题目正序第一题
     *
     * @param examinationId
     * @return
     */
    public Optional<ExaminationSubject> findFirstByExaminationId(Long examinationId) {
        if (ParamsUtil.isEmpty(examinationId)) {
            return Optional.empty();
        }
        EntityWrapper<ExaminationSubject> ew = EntityWrapperUtil.build();
        ew.orderAsc(Arrays.asList("subject_id"));
        ew.eq("examination_id", examinationId);
        return selectPage(new Page<>(1, 1), ew).getRecords().stream().findAny();
    }

    /**
     * 查询上一题
     *
     * @param examinationId
     * @param subjectId
     * @return
     */
    public Optional<ExaminationSubject> findPrevious(Long examinationId, Long subjectId) {
        if (ParamsUtil.isEmpty(examinationId) || ParamsUtil.isEmpty(subjectId)) {
            return Optional.empty();
        }
        /*
            select subject_id 上一题 from exam_examination_subject
            where subject_id < 590971026880466944 and examination_id = 590969316204220416
            order by subject_id desc
            limit 0, 1;
         */
        EntityWrapper<ExaminationSubject> ew = EntityWrapperUtil.build();
        ew.orderDesc(Arrays.asList("subject_id"));
        ew.eq("examination_id", examinationId);
        ew.lt("subject_id", subjectId);
        return selectPage(new Page<>(1, 1), ew).getRecords().stream().findAny();
    }

    /**
     * 查询下一题
     *
     * @param examinationId
     * @param subjectId
     * @return
     */
    public Optional<ExaminationSubject> findNext(Long examinationId, Long subjectId) {
        if (ParamsUtil.isEmpty(examinationId) || ParamsUtil.isEmpty(subjectId)) {
            return Optional.empty();
        }
        /*
            select subject_id 下一题 from exam_examination_subject
            where subject_id > 590971026880466944 and examination_id = 590969316204220416
            order by subject_id asc
            limit 0, 1;
         */
        EntityWrapper<ExaminationSubject> ew = EntityWrapperUtil.build();
        ew.orderAsc(Arrays.asList("subject_id"));
        ew.eq("examination_id", examinationId);
        ew.gt("subject_id", subjectId);
        return selectPage(new Page<>(1, 1), ew).getRecords().stream().findAny();
    }

    /**
     * 查询
     *
     * @param examinationId
     * @param subjectId
     * @return
     */
    public Optional<ExaminationSubject> find(Long examinationId, Long subjectId) {
        if (ParamsUtil.isEmpty(examinationId) || ParamsUtil.isEmpty(subjectId)) {
            return Optional.empty();
        }
        EntityWrapper<ExaminationSubject> ew = EntityWrapperUtil.build();
        ew.eq("examination_id", examinationId);
        ew.eq("subject_id", subjectId);
        return selectList(ew).stream().findAny();
    }
}
