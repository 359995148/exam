package com.github.fanzh.exam.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.entity.PageEntity;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.OptionalUtil;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.exam.api.dto.AnswerDto;
import com.github.fanzh.exam.api.dto.RankInfoDto;
import com.github.fanzh.exam.api.dto.SubjectDto;
import com.github.fanzh.exam.api.module.Answer;
import com.github.fanzh.exam.service.AnswerService;
import com.github.fanzh.exam.service.SubjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 答题controller
 *
 * @author fanzh
 * @date 2018/11/8 21:24
 */
@Slf4j
@AllArgsConstructor
@Api("答题信息管理")
@RestController
@RequestMapping("/v1/answer")
public class AnswerController {

    private final AnswerService answerService;

    private final SubjectService subjectService;


    @ApiOperation(value = "答题查询")
    @GetMapping("/{id}")
    public ExecResult<Answer> get(@PathVariable Long id) {
        return ExecResultUtil.success(answerService.baseGetById(id));
    }

    @ApiOperation(value = "答题列表或分页查询")
    @GetMapping("answerList")
    public ExecResult<Page<Answer>> listOrPage(PageEntity pageEntity, Answer answer) {
        answer.setTenantCode(SysUtil.getTenantCode());
        return ExecResultUtil.success(answerService.baseListOrPage( OptionalUtil.build(pageEntity) , answer ));
    }

    @ApiOperation(value = "答题新增")
    @PostMapping
    public ExecResult<Boolean> add(@RequestBody @Valid Answer answer) {
        return answerService.addAnswer(answer);
    }

    @ApiOperation(value = "答题更新")
    @PutMapping
    public ExecResult<Boolean> update(@RequestBody @Valid Answer answer) {
        return answerService.updateAnswer(answer);
    }

    @ApiOperation(value = "答题批改")
    @PutMapping("mark")
    public ExecResult<Boolean> mark(@RequestBody @Valid Answer answer) {
        return answerService.markAnswer(answer);
    }

    @ApiOperation(value = "答题删除")
    @DeleteMapping("{id}")
    public ExecResult<Boolean> delete(@PathVariable Long id) {
        answerService.baseLogicDelete(id);
        return ExecResultUtil.success(true);
    }

    @ApiOperation(value = "保存答题")
    @PostMapping("save")
    public ExecResult<Boolean> save(@RequestBody @Valid Answer answer) {
        answerService.baseSave(answer);
        return ExecResultUtil.success(true);
    }

    @ApiOperation(value = "答题保存")
    @PostMapping("saveAnswer")
    public ExecResult<Boolean> saveAnswer(@RequestBody AnswerDto answer) {
        answerService.saveAnswerDto(answer);
        return ExecResultUtil.success(true);
    }

    @ApiOperation(value = "保存答题，并返回下一题")
    @PostMapping("saveAndNext")
    public ExecResult<SubjectDto> saveAndNext(
            @RequestParam(value = "nextType") @ApiParam(value = "0：下一题，1：上一题，2：提交", required = true) Integer nextType
            , @RequestParam(value = "nextSubjectId", required = false) @ApiParam(value = "下一题ID") Long nextSubjectId
            , @RequestParam(value = "nextSubjectType", required = false) @ApiParam(value = "下一题的类型 选择题、判断题") Integer nextSubjectType
            , @RequestBody AnswerDto answer
    ) {
        return answerService.saveAndNext(
                nextType
                , OptionalUtil.build(nextSubjectId)
                , OptionalUtil.build(nextSubjectType)
                , answer
        );
    }

    @ApiOperation(value = "保存答题，返回下一题信息")
    @PostMapping("anonymousUser/saveAndNext")
    public ExecResult<SubjectDto> anonymousUserSaveAndNext(
            @RequestParam(value = "nextType") @ApiParam(value = "0：下一题，1：上一题，2：提交", required = true) Integer nextType
            , @RequestParam(value = "nextSubjectId", required = false) @ApiParam(value = "下一题ID") Long nextSubjectId
            , @RequestParam(value = "nextSubjectType", required = false) @ApiParam(value = "下一题的类型 选择题、判断题") Integer nextSubjectType
            , @RequestBody AnswerDto answer
    ) {
        return answerService.saveAndNext(
                nextType
                , OptionalUtil.build(nextSubjectId)
                , OptionalUtil.build(nextSubjectType)
                , answer
        );
    }

    @ApiOperation(value = "下一题查询")
    @GetMapping("nextSubject")
    public ExecResult<SubjectDto> nextSubject(
            @RequestParam(value = "examinationId") @ApiParam(value = "考试ID", required = true) Long examinationId
            , @RequestParam(value = "subjectId") @ApiParam(value = "题目ID", required = true) Long subjectId
            , @RequestParam(value = "type") @ApiParam(value = "下一题ID", required = true) Integer type
            , @RequestParam(value = "nextType") @ApiParam(value = "0：下一题，1：上一题", required = true) Integer nextType
    ) {
        return ExecResultUtil.success(subjectService.getNextByCurrentIdAndType(examinationId, subjectId, type, nextType));
    }

    @ApiOperation(value = "答卷提交")
    @PostMapping("submit")
    public ExecResult<Boolean> submit(@RequestBody Answer answer) {
        return answerService.submitAsync(answer);
    }

    @ApiOperation(value = "答卷提交")
    @PostMapping("anonymousUser/submit")
    public ExecResult<Boolean> anonymousUserSubmit(@RequestBody Answer answer) {
        return answerService.submitAsync(answer);
    }

    @ApiOperation(value = "答题信息列表详细信息查询")
    @GetMapping("record/{recordId}/answerListInfo")
    public Page<AnswerDto> answerListInfo(
            @RequestParam(value = CommonConstant.PAGE_NUM, required = false, defaultValue = CommonConstant.PAGE_NUM_DEFAULT) @ApiParam(value = "页数") Integer pageNum
            , @RequestParam(value = CommonConstant.PAGE_SIZE, required = false, defaultValue = CommonConstant.PAGE_SIZE_DEFAULT) @ApiParam(value = "页长") Integer pageSize
            , @RequestParam(value = CommonConstant.SORT, required = false, defaultValue = CommonConstant.PAGE_SORT_DEFAULT) @ApiParam(value = "排序字段") String sort
            , @RequestParam(value = CommonConstant.ORDER, required = false, defaultValue = CommonConstant.PAGE_ORDER_DEFAULT) @ApiParam(value = "排序方式") String order
            , @PathVariable Long recordId
            , Answer answer
    ) {
        return answerService.answerInfoListOrPage(
                recordId
                , OptionalUtil.build(pageNum)
                , OptionalUtil.build(pageSize)
                , OptionalUtil.build(sort)
                , OptionalUtil.build(order)
                , answer
        );
    }

    @ApiImplicitParam(name = "recordId", value = "考试记录id", dataType = "Long")
    @ApiOperation(value = "答题详情")
    @GetMapping("record/{recordId}/answerInfo")
    public ExecResult<AnswerDto> answerInfo(
            @PathVariable Long recordId
            , @RequestParam(value = "currentSubjectId", required = false) @ApiParam(value = "当前题目ID") Long currentSubjectId
            , @RequestParam(value = "nextSubjectType", required = false) @ApiParam(value = "下一题的类型") Integer nextSubjectType
            , @RequestParam(value = "nextType", required = false) @ApiParam(value = "0：下一题，1：上一题") Integer nextType
    ) {
        return answerService.answerInfo(
                recordId
                , OptionalUtil.build(currentSubjectId)
                , OptionalUtil.build(nextSubjectType)
                , OptionalUtil.build(nextType)
        );
    }

    @ApiImplicitParam(name = "recordId", value = "考试记录id", dataType = "Long")
    @ApiOperation(value = "排名列表")
    @GetMapping("record/{recordId}/rankInfo")
    public ExecResult<List<RankInfoDto>> rankInfo(@PathVariable Long recordId) {
        return answerService.getRankInfo(recordId);
    }

    /**
     * 移动端提交答题
     *
     * @param examinationId examinationId
     * @return ResponseBean
     * @author fanzh
     * @date 2020/03/15 16:08
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "examinationId", value = "考试id", dataType = "Long"),
            @ApiImplicitParam(name = "identifier", value = "考生账号", dataType = "String")
    })
    @ApiOperation(value = "移动端答题提交")
    @PostMapping("anonymousUser/submitAll/{examinationId}")
    public ExecResult<Boolean> anonymousUserSubmitAll(
            @PathVariable Long examinationId
            , @RequestParam(value = "identifier") @ApiParam(value = "考生账号", required = true) String identifier
            , @RequestBody List<SubjectDto> subjectDtos
    ) {
        return answerService.anonymousUserSubmit(examinationId, identifier, subjectDtos);
    }
}
