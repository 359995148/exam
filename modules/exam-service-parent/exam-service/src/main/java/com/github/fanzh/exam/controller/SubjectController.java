package com.github.fanzh.exam.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.entity.PageEntity;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.utils.OptionalUtil;
import com.github.fanzh.common.basic.utils.PageUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.common.core.utils.excel.ExcelToolUtil;
import com.github.fanzh.common.log.annotation.Log;
import com.github.fanzh.common.security.annotations.AdminTenantTeacherAuthorization;
import com.github.fanzh.exam.api.dto.SubjectDto;
import com.github.fanzh.exam.api.module.ExaminationSubject;
import com.github.fanzh.exam.excel.listener.SubjectImportListener;
import com.github.fanzh.exam.excel.model.SubjectExcelModel;
import com.github.fanzh.exam.service.AnswerService;
import com.github.fanzh.exam.service.ExaminationSubjectService;
import com.github.fanzh.exam.service.SubjectService;
import com.github.fanzh.exam.utils.SubjectUtil;
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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 题目controller
 *
 * @author fanzh
 * @date 2018/11/8 21:29
 */
@Slf4j
@AllArgsConstructor
@Api("题目信息管理")
@RestController
@RequestMapping("/v1/subject")
public class SubjectController {

    private final SubjectService subjectService;

    private final AnswerService answerService;

    private final ExaminationSubjectService examinationSubjectService;

    /**
     * 根据ID获取
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:43
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取题目信息", notes = "根据题目id获取题目详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "题目ID", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "type", value = "题目类型", required = true, dataType = "Integer")})
    public ExecResult<SubjectDto> subject(@PathVariable Long id, @RequestParam Integer type) {
        return new ExecResult<>(subjectService.get(id, type));
    }

    /**
     * 获取分页数据
     *
     * @param pageEntity
     * @param subject
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = CommonConstant.PAGE_NUM, value = "分页页码", defaultValue = CommonConstant.PAGE_NUM_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.PAGE_SIZE, value = "分页大小", defaultValue = CommonConstant.PAGE_SIZE_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.SORT, value = "排序字段", defaultValue = CommonConstant.PAGE_SORT_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.ORDER, value = "排序方向", defaultValue = CommonConstant.PAGE_ORDER_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = "subject", value = "题目信息", dataType = "Subject")
    })
    @ApiOperation(value = "获取题目列表")
    @GetMapping("subjectList")
    public ExecResult<Page<SubjectDto>> subjectList(PageEntity pageEntity, SubjectDto subject) {
        ExaminationSubject examinationSubject = new ExaminationSubject();
        examinationSubject.setCategoryId(subject.getCategoryId());
        examinationSubject.setExaminationId(subject.getExaminationId());
        Page<ExaminationSubject> pageSource = examinationSubjectService.baseListOrPage(OptionalUtil.build(pageEntity), examinationSubject);
        if (ParamsUtil.isEmpty(pageSource.getRecords())) {
            return ExecResultUtil.success(PageUtil.copy(pageSource));
        }
        List<SubjectDto> subjectDtos = new ArrayList<>();
        pageSource.getRecords().forEach(tempExaminationSubject -> {
            SubjectDto tempSubjectDto = subjectService.get(tempExaminationSubject.getSubjectId(), tempExaminationSubject.getType());
            if (tempSubjectDto != null) {
                subjectDtos.add(tempSubjectDto);
            }
        });
        Page<SubjectDto> page = PageUtil.copy(pageSource);
        page.setRecords(subjectDtos);
        return ExecResultUtil.success(page);
    }

    /**
     * 创建
     *
     * @param subject subject
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:43
     */
    @PostMapping
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "创建题目", notes = "创建题目")
    @ApiImplicitParam(name = "subject", value = "题目信息", required = true, dataType = "SubjectDto")
    @Log("新增题目")
    public ExecResult<SubjectDto> addSubject(@RequestBody @Valid SubjectDto subject) {
        subjectService.addSubject(Arrays.asList(subject));
        return ExecResultUtil.success(subject);
    }

    /**
     * 更新
     *
     * @param subject subject
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:43
     */
    @Log("更新题目")
    @ApiImplicitParam(name = "subject", value = "角色实体subject", required = true, dataType = "Subject")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "更新题目信息", notes = "根据题目id更新题目的基本信息")
    @PutMapping
    public ExecResult<SubjectDto> updateSubject(@RequestBody @Valid SubjectDto subject) {
        subjectService.updateSubject(subject);
        return ExecResultUtil.success(subject);
    }

    /**
     * 删除
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:43
     */
    @Log("删除题目")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "题目ID", required = true, dataType = "Long", paramType = "path")
    })
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "删除题目", notes = "根据ID删除题目")
    @DeleteMapping("{id}")
    public ExecResult<Boolean> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(SetUtil.build(id));
        return ExecResultUtil.success(true);
    }

    /**
     * 批量删除
     *
     * @param ids ids
     * @return ResponseBean
     * @author fanzh
     * @date 2018/12/04 9:55
     */
    @PostMapping("deleteAll")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "批量删除题目", notes = "根据题目id批量删除题目")
    @ApiImplicitParam(name = "ids", value = "题目ID", dataType = "Long")
    @Log("批量删除题目")
    public ExecResult<Boolean> deleteSubjects(@RequestBody List<Long> ids) {
        subjectService.deleteSubject(SetUtil.build(ids));
        return ExecResultUtil.success(true);
    }

    /**
     * 查询题目和答题
     *
     * @param subjectId       subjectId
     * @param examRecordId    examRecordId
     * @param userId          userId
     * @param nextType        -1：当前题目，0：下一题，1：上一题
     * @param nextSubjectId   nextSubjectId
     * @param nextSubjectType 下一题的类型，选择题、判断题
     * @return ResponseBean
     * @author fanzh
     * @date 2019/01/16 22:25
     */
    @GetMapping("subjectAnswer")
    @ApiOperation(value = "查询题目和答题", notes = "根据题目id查询题目和答题")
    @ApiImplicitParams({@ApiImplicitParam(name = "subjectId", value = "题目ID", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "examRecordId", value = "考试记录ID", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "userId", value = "用户ID", dataType = "String"),
            @ApiImplicitParam(name = "nextType", value = "-1：当前题目，0：下一题，1：上一题", dataType = "Integer")})
    public ExecResult<SubjectDto> subjectAnswer(
            @RequestParam("subjectId") @NotBlank Long subjectId
            , @RequestParam("examRecordId") @NotBlank Long examRecordId
            , @RequestParam(value = "userId", required = false) String userId
            , @RequestParam Integer nextType
            , @RequestParam(required = false) Long nextSubjectId
            , @RequestParam(required = false) Integer nextSubjectType
    ) {
        return ExecResultUtil.success(answerService.subjectAnswer(subjectId, examRecordId, nextType, OptionalUtil.build(nextSubjectId), OptionalUtil.build(nextSubjectType)));
    }

    /**
     * 查询题目和答题
     *
     * @param subjectId       subjectId
     * @param examRecordId    examRecordId
     * @param userId          userId
     * @param nextType        -1：当前题目，0：下一题，1：上一题
     * @param nextSubjectId   nextSubjectId
     * @param nextSubjectType 下一题的类型，选择题、判断题
     * @return ResponseBean
     * @author fanzh
     * @date 2019/01/16 22:25
     */
    @GetMapping("anonymousUser/subjectAnswer")
    @ApiOperation(value = "查询题目和答题", notes = "根据题目id查询题目和答题")
    @ApiImplicitParams({@ApiImplicitParam(name = "subjectId", value = "题目ID", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "examRecordId", value = "考试记录ID", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "userId", value = "用户ID", dataType = "String"),
            @ApiImplicitParam(name = "nextType", value = "-1：当前题目，0：下一题，1：上一题", dataType = "Integer")})
    public ExecResult<SubjectDto> anonymousUserSubjectAnswer(
            @RequestParam("subjectId") @NotBlank Long subjectId
            , @RequestParam("examRecordId") @NotBlank Long examRecordId
            , @RequestParam(value = "userId", required = false) String userId
            , @RequestParam Integer nextType
            , @RequestParam(required = false) Long nextSubjectId
            , @RequestParam(required = false) Integer nextSubjectType
    ) {
        return ExecResultUtil.success(answerService.subjectAnswer(subjectId, examRecordId, nextType, OptionalUtil.build(nextSubjectId), OptionalUtil.build(nextSubjectType)));
    }


    /**
     * 导出题目
     *
     * @param ids ids
     * @author fanzh
     * @date 2018/11/28 12:53
     */
    @PostMapping("export")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "导出题目", notes = "根据分类id导出题目")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "题目ID", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "examinationId", value = "考试ID", dataType = "Long"),
            @ApiImplicitParam(name = "categoryId", value = "分类ID", dataType = "Long")
    })
    @Log("导出题目")
    public void exportSubject(
            @RequestBody List<Long> ids
            , @RequestParam(required = false) Long examinationId
            , @RequestParam(required = false) Long categoryId
            , HttpServletRequest request
            , HttpServletResponse response
    ) {
        List<SubjectDto> subjects = subjectService.exportSubject(ids, examinationId, categoryId);
        ExcelToolUtil.exportExcel(request, response, SubjectUtil.convertToExcelModel(subjects), SubjectExcelModel.class);
    }

    /**
     * 导入数据
     *
     * @param examinationId examinationId
     * @param categoryId    categoryId
     * @param file          file
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/28 12:59
     */
    @Log("导入题目")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "examinationId", value = "考试ID", dataType = "Long"),
            @ApiImplicitParam(name = "categoryId", value = "分类ID", dataType = "Long")
    })
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "导入题目", notes = "导入题目")
    @RequestMapping("import")
    public ExecResult<Boolean> importSubject(Long examinationId, Long categoryId, @ApiParam(value = "要上传的文件", required = true) MultipartFile file) {
        ExcelToolUtil.importExcel(file, SubjectExcelModel.class, new SubjectImportListener(subjectService, examinationId, categoryId));
        return ExecResultUtil.success(true);
    }
}
