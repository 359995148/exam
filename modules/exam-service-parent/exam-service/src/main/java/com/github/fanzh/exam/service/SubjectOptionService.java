package com.github.fanzh.exam.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.fanzh.exam.api.module.SubjectOption;
import com.github.fanzh.exam.mapper.SubjectOptionMapper;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.basic.utils.EntityWrapperUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 选择题选项service
 *
 * @author fanzh
 * @date 2019/6/16 15:01
 */
@Service
public class SubjectOptionService extends BaseService<SubjectOptionMapper, SubjectOption> {

    public List<SubjectOption> findBySubjectChoicesId(Long id) {
        if (ParamsUtil.isEmpty(id)) {
            return Collections.emptyList();
        }
        return findBySubjectChoicesId(SetUtil.build(id));
    }

    public List<SubjectOption> findBySubjectChoicesId(Set<Long> id) {
        if (ParamsUtil.isEmpty(id)) {
            return Collections.emptyList();
        }
        EntityWrapper<SubjectOption> ew = EntityWrapperUtil.build();
        ew.in("subject_choices_id", id);
        return selectList(ew);
    }

    public void deleteBySubjectChoicesId(Set<Long> id) {
        if (ParamsUtil.isEmpty(id)) {
            return;
        }
        List<SubjectOption> list = findBySubjectChoicesId(id);
        baseDelete(list.stream().map(o -> o.getId()).collect(Collectors.toSet()));
    }

}
