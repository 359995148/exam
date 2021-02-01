package com.github.fanzh.exam.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.common.basic.vo.AttachmentVo;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.entity.PageEntity;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.OptionalUtil;
import com.github.fanzh.common.basic.utils.PageUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.common.log.annotation.Log;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.exam.api.dto.KnowledgeDto;
import com.github.fanzh.exam.api.module.Knowledge;
import com.github.fanzh.exam.service.KnowledgeService;
import com.github.fanzh.user.api.feign.UserServiceClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 知识库controller
 *
 * @author fanzh
 * @date 2019/1/1 15:11
 */
@Slf4j
@AllArgsConstructor
@Api("知识库信息管理")
@RestController
@RequestMapping("/v1/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    private final UserServiceClient userServiceClient;

    /**
     * 根据ID获取
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2019/1/1 15:15
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取知识信息", notes = "根据知识id获取知识详细信息")
    @ApiImplicitParam(name = "id", value = "知识ID", required = true, dataType = "Long", paramType = "path")
    public ExecResult<Knowledge> knowledge(@PathVariable Long id) {
        return ExecResultUtil.success(knowledgeService.baseGetById(id));
    }

    /**
     * 获取分页数据
     *
     * @param pageEntity
     * @param knowledge
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = CommonConstant.PAGE_NUM, value = "分页页码", defaultValue = CommonConstant.PAGE_NUM_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.PAGE_SIZE, value = "分页大小", defaultValue = CommonConstant.PAGE_SIZE_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.SORT, value = "排序字段", defaultValue = CommonConstant.PAGE_SORT_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.ORDER, value = "排序方向", defaultValue = CommonConstant.PAGE_ORDER_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = "knowledge", value = "知识信息", dataType = "Knowledge")
    })
    @ApiOperation(value = "获取知识列表")
    @GetMapping("knowledgeList")
    public ExecResult<Page<KnowledgeDto>> knowledgeList(PageEntity pageEntity, Knowledge knowledge) {
        knowledge.setTenantCode(SysUtil.getTenantCode());
        // 查询知识
        Page<Knowledge> pageSource = knowledgeService.baseListOrPage(OptionalUtil.build(pageEntity), knowledge);
        if (ParamsUtil.isEmpty(pageSource.getRecords())) {
            return ExecResultUtil.success(PageUtil.copy(pageSource));
        }
        // 根据附件ID查询附件
        ExecResult<List<AttachmentVo>> attachmentRet = userServiceClient.findAttachmentById(pageSource.getRecords().stream().map(o -> o.getAttachmentId()).distinct().collect(Collectors.toList()));
        List<AttachmentVo> attachmentVoList = new ArrayList<>();
        if (attachmentRet.isSuccess() && ParamsUtil.isNotEmpty(attachmentRet.getData())) {
            attachmentVoList = attachmentRet.getData();
        }
        List<AttachmentVo> finalAttachmentVoList = attachmentVoList;
        List<KnowledgeDto> knowledgeDtoList = new ArrayList<>();
        pageSource.getRecords().stream()
                // 转成Dto
                .map(tempKnowledge -> {
                    KnowledgeDto knowledgeDto = new KnowledgeDto();
                    BeanUtils.copyProperties(tempKnowledge, knowledgeDto);
                    return knowledgeDto;
                })
                // 遍历
                .forEach(tempKnowledgeDto -> {
                    Optional<AttachmentVo> tempAttachmentOpt = finalAttachmentVoList.stream()
                            // 根据ID过滤
                            .filter(tempAttachmentVo -> Objects.equals(tempAttachmentVo.getId(), tempKnowledgeDto.getAttachmentId()))
                            // 匹配第一个
                            .findAny();
                    // 设置附件名称、附件大小
                    if (tempAttachmentOpt.isPresent()) {
                        tempKnowledgeDto.setAttachName(tempAttachmentOpt.get().getAttachName());
                        tempKnowledgeDto.setAttachSize(tempAttachmentOpt.get().getAttachSize());
                    }
                    knowledgeDtoList.add(tempKnowledgeDto);
                });
        Page<KnowledgeDto> page = PageUtil.copy(pageSource);
        page.setRecords(knowledgeDtoList);
        return ExecResultUtil.success(page);
    }

    /**
     * 创建
     *
     * @param knowledge knowledge
     * @return ResponseBean
     * @author fanzh
     * @date 2019/1/1 15:15
     */
    @ApiImplicitParam(name = "knowledge", value = "知识实体knowledge", required = true, dataType = "Knowledge")
    @ApiOperation(value = "创建知识", notes = "创建知识")
    @PostMapping
    public ExecResult<Boolean> addKnowledge(@RequestBody @Valid Knowledge knowledge) {
        knowledgeService.baseSave(knowledge);
        return ExecResultUtil.success(true);
    }
    /**
     * 更新
     *
     * @param knowledge knowledge
     * @return ResponseBean
     * @author fanzh
     * @date 2019/1/1 15:15
     */
    @ApiImplicitParam(name = "knowledge", value = "知识实体knowledge", required = true, dataType = "Knowledge")
    @ApiOperation(value = "更新知识信息", notes = "根据知识id更新知识的基本信息")
    @PutMapping
    public ExecResult<Boolean> updateKnowledge(@RequestBody @Valid Knowledge knowledge) {
        knowledgeService.baseUpdate(knowledge);
        return ExecResultUtil.success(true);
    }

    /**
     * 删除
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2019/1/1 15:15
     */
    @ApiImplicitParam(name = "id", value = "知识ID", required = true, paramType = "path")
    @ApiOperation(value = "删除知识", notes = "根据ID删除知识")
    @DeleteMapping("{id}")
    public ExecResult<Boolean> deleteKnowledge(@PathVariable Long id) {
        knowledgeService.deleteKnowledge(SetUtil.build(id));
        return ExecResultUtil.success(true);
    }

    /**
     * 批量删除
     *
     * @param ids ids
     * @return ResponseBean
     * @author fanzh
     * @date 2019/1/1 15:15
     */
    @PostMapping("deleteAll")
    @ApiOperation(value = "批量删除知识", notes = "根据知识id批量删除知识")
    @ApiImplicitParam(name = "ids", value = "知识ID", dataType = "Long")
    @Log("批量删除知识")
    public ExecResult<Boolean> deleteAllKnowledge(@RequestBody List<Long> ids) {
        knowledgeService.deleteKnowledge(SetUtil.build(ids));
        return ExecResultUtil.success(true);
    }
}
