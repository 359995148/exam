package com.github.fanzh.exam.service;

import com.github.fanzh.exam.mapper.KnowledgeMapper;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.exceptions.CommonException;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.exam.api.module.Knowledge;
import com.github.fanzh.user.api.feign.UserServiceClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 知识库service
 *
 * @author fanzh
 * @date 2019/1/1 15:09
 */
@AllArgsConstructor
@Service
public class KnowledgeService extends BaseService<KnowledgeMapper, Knowledge> {

    private final UserServiceClient userServiceClient;

    @Transactional(rollbackFor = Throwable.class)
    public void deleteKnowledge(Set<Long> id) {
        List<Knowledge> knowledgeList = this.baseFindById(id);
        if (ParamsUtil.isEmpty(knowledgeList)) {
            return;
        }
        Set<Long> ids = knowledgeList.stream().map(o -> o.getId()).collect(Collectors.toSet());
        this.baseLogicDelete(ids);
        Set<Long> attachmentIds = knowledgeList.stream().map(o -> o.getAttachmentId()).collect(Collectors.toSet());
        ExecResult<Boolean> ret = userServiceClient.deleteAttachment(new ArrayList<>(attachmentIds));
        if (ret.isError()) {
            throw new CommonException("Method deleteKnowledge() delete attachment failed: " + ret.getMsg());
        }
    }
}
