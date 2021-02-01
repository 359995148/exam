package com.github.fanzh.exam.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.common.core.utils.excel.ExcelToolUtil;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.constant.SqlField;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.entity.PageEntity;
import com.github.fanzh.common.core.utils.DateUtils;
import com.github.fanzh.common.basic.utils.EntityWrapperUtil;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.JsonUtil;
import com.github.fanzh.common.core.utils.OptionalUtil;
import com.github.fanzh.common.basic.utils.PageUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.log.annotation.Log;
import com.github.fanzh.common.security.annotations.AdminTenantTeacherAuthorization;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.exam.api.dto.ExaminationDashboardDto;
import com.github.fanzh.exam.api.dto.ExaminationRecordDto;
import com.github.fanzh.exam.api.dto.StartExamDto;
import com.github.fanzh.exam.api.enums.SubmitStatusEnum;
import com.github.fanzh.exam.api.module.Examination;
import com.github.fanzh.exam.api.module.ExaminationRecord;
import com.github.fanzh.exam.excel.model.ExamRecordExcelModel;
import com.github.fanzh.exam.service.AnswerService;
import com.github.fanzh.exam.service.ExamRecordService;
import com.github.fanzh.exam.service.ExaminationService;
import com.github.fanzh.exam.utils.ExamRecordUtil;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 考试记录controller
 *
 * @author fanzh
 * @date 2018/11/8 21:27
 */
@Slf4j
@AllArgsConstructor
@Api("考试记录信息管理")
@RestController
@RequestMapping("/v1/examRecord")
public class ExamRecordController {

    private final ExamRecordService examRecordService;

    private final AnswerService answerService;

    private final ExaminationService examinationService;

    /**
     * 根据ID获取
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:33
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取考试记录信息", notes = "根据考试记录id获取考试记录详细信息")
    @ApiImplicitParam(name = "id", value = "考试记录ID", required = true, dataType = "Long", paramType = "path")
    public ExecResult<ExaminationRecord> examRecord(@PathVariable Long id) {
        return ExecResultUtil.success(examRecordService.baseGetById(id));
    }

    /**
     * 获取分页数据
     *
     * @param pageNum    pageNum
     * @param pageSize   pageSize
     * @param sort       sort
     * @param order      order
     * @param examRecord examRecord
     * @return PageInfo
     * @author fanzh
     * @date 2018/11/10 21:33
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = CommonConstant.PAGE_NUM, value = "分页页码", defaultValue = CommonConstant.PAGE_NUM_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.PAGE_SIZE, value = "分页大小", defaultValue = CommonConstant.PAGE_SIZE_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.SORT, value = "排序字段", defaultValue = CommonConstant.PAGE_SORT_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.ORDER, value = "排序方向", defaultValue = CommonConstant.PAGE_ORDER_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = "examRecord", value = "考试记录信息", dataType = "ExamRecord")
    })
    @ApiOperation(value = "获取考试记录列表")
    @GetMapping("examRecordList")
    public ExecResult<Page<ExaminationRecordDto>> examRecordList(PageEntity pageEntity, ExaminationRecord examRecord) {
        examRecord.setTenantCode(SysUtil.getTenantCode());
        Page<ExaminationRecord> pageSource = examRecordService.baseListOrPage(OptionalUtil.build(pageEntity), examRecord);
        if (ParamsUtil.isEmpty(pageSource.getRecords())) {
            return ExecResultUtil.success(PageUtil.copy(pageSource));
        }
        // 查询考试信息
        List<Examination> examinations = examinationService.baseFindById(pageSource.getRecords().stream().map(o -> o.getExaminationId()).collect(Collectors.toSet()));

        List<ExaminationRecordDto> examRecordDtoList = new ArrayList<>();
        pageSource.getRecords().forEach(tempExamRecord -> {
            // 找到考试记录所属的考试信息
            Optional<Examination> examinationRecordExamination = examinations.stream()
                    .filter(tempExamination -> tempExamRecord.getExaminationId().equals(tempExamination.getId()))
                    .findAny();
            // 转换成ExamRecordDto
            if (examinationRecordExamination.isPresent()) {
                ExaminationRecordDto examRecordDto = new ExaminationRecordDto();
                BeanUtils.copyProperties(examinationRecordExamination.get(), examRecordDto);
                examRecordDto.setId(tempExamRecord.getId());
                examRecordDto.setStartTime(tempExamRecord.getStartTime());
                examRecordDto.setEndTime(tempExamRecord.getEndTime());
                examRecordDto.setScore(tempExamRecord.getScore());
                examRecordDto.setUserId(tempExamRecord.getUserId());
                examRecordDto.setExaminationId(tempExamRecord.getExaminationId());
                // 正确题目数
                examRecordDto.setCorrectNumber(tempExamRecord.getCorrectNumber());
                examRecordDto.setInCorrectNumber(tempExamRecord.getIncorrectNumber());
                // 提交状态
                examRecordDto.setSubmitStatus(tempExamRecord.getSubmitStatus());
                examRecordDtoList.add(examRecordDto);
            }
        });
        examRecordService.fillUserDept(examRecordDtoList, pageSource.getRecords().stream().map(o -> o.getUserId()).collect(Collectors.toSet()));
        Page<ExaminationRecordDto> page = PageUtil.copy(pageSource);
        page.setRecords(examRecordDtoList);
        return ExecResultUtil.success(page);
    }

    /**
     * 创建
     *
     * @param examRecord examRecord
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:33
     */
    @Log("新增考试记录")
    @ApiOperation(value = "创建考试记录", notes = "创建考试记录")
    @ApiImplicitParam(name = "examRecord", value = "考试记录实体examRecord", required = true, dataType = "ExamRecord")
    @PostMapping
    public ExecResult<ExaminationRecord> addExamRecord(@RequestBody @Valid ExaminationRecord examRecord) {
        examRecord.setStartTime(Calendar.getInstance().getTime());
        examRecordService.baseSave(examRecord);
        return ExecResultUtil.success(examRecord);
    }

    /**
     * 更新
     *
     * @param examRecord examRecord
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:34
     */
    @Log("更新考试记录")
    @ApiImplicitParam(name = "examRecord", value = "考试记录实体examRecord", required = true, dataType = "ExamRecord")
    @ApiOperation(value = "更新考试记录信息", notes = "根据考试记录id更新考试记录的基本信息")
    @PutMapping
    public ExecResult<Boolean> updateExamRecord(@RequestBody @Valid ExaminationRecord examRecord) {
        examRecordService.baseUpdate(examRecord);
        return ExecResultUtil.success(true);
    }

    /**
     * 删除
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:34
     */
    @Log("删除考试记录")
    @ApiImplicitParam(name = "id", value = "考试记录ID", required = true, paramType = "path")
    @ApiOperation(value = "删除考试记录", notes = "根据ID删除考试记录")
    @DeleteMapping("{id}")
    public ExecResult<Boolean> deleteExamRecord(@PathVariable Long id) {
        examRecordService.baseLogicDelete(id);
        return ExecResultUtil.success(true);
    }

    /**
     * 导出
     *
     * @param ids      ids
     * @param request  request
     * @param response response
     * @author fanzh
     * @date 2018/12/31 22:28
     */
    @Log("导出考试记录")
    @ApiImplicitParam(name = "ids", value = "成绩ID", required = true, dataType = "Long")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "导出考试成绩", notes = "根据成绩id导出成绩")
    @PostMapping("export")
    public void exportExamRecord(@RequestBody List<Long> ids, HttpServletRequest request, HttpServletResponse response) {
        EntityWrapper<ExaminationRecord> ew = EntityWrapperUtil.build();
        if (ParamsUtil.isNotEmpty(ids)) {
            ew.in(SqlField.ID, ids);
        } else {
            ew.eq(SqlField.TENANT_CODE, SysUtil.getTenantCode());
        }
        List<ExaminationRecord> examRecordList = examRecordService.selectList(ew);
        if (ParamsUtil.isEmpty(examRecordList)) {
            return;
        }
        // 查询考试、用户、部门数据
        List<ExaminationRecordDto> examRecordDtoList = new ArrayList<>();
        // 查询考试信息
        List<Examination> examinationList = examinationService.baseFindById(examRecordList.stream().map(o -> o.getExaminationId()).collect(Collectors.toSet()));
        // 用户id
        Set<Long> userIdSet = new HashSet<>();
        examRecordList.forEach(tempExamRecord -> {
            // 查找考试记录所属的考试信息
            Optional<Examination> examinationOpt = examinationList.stream()
                    .filter(tempExamination -> tempExamRecord.getExaminationId().equals(tempExamination.getId()))
                    .findAny();
            if (!examinationOpt.isPresent()) {
                return;
            }
            ExaminationRecordDto recordDto = new ExaminationRecordDto();
            recordDto.setId(tempExamRecord.getId());
            recordDto.setExaminationName(examinationOpt.get().getExaminationName());
            recordDto.setStartTime(tempExamRecord.getStartTime());
            recordDto.setEndTime(tempExamRecord.getEndTime());
            recordDto.setDuration(ExamRecordUtil.getExamDuration(tempExamRecord.getStartTime(), tempExamRecord.getEndTime()));
            recordDto.setScore(tempExamRecord.getScore());
            recordDto.setUserId(tempExamRecord.getUserId());
            recordDto.setCorrectNumber(tempExamRecord.getCorrectNumber());
            recordDto.setInCorrectNumber(tempExamRecord.getIncorrectNumber());
            recordDto.setSubmitStatusName(
                    SubmitStatusEnum.match(tempExamRecord.getSubmitStatus(), SubmitStatusEnum.NOT_SUBMITTED).getName());
            userIdSet.add(tempExamRecord.getUserId());
            examRecordDtoList.add(recordDto);
        });
        examRecordService.fillUserDept(examRecordDtoList, userIdSet);
        ExcelToolUtil.exportExcel(request, response, JsonUtil.listToList(examRecordDtoList, ExamRecordExcelModel.class), ExamRecordExcelModel.class);
    }

    /**
     * 开始考试
     *
     * @param examRecord examRecord
     * @return ResponseBean
     * @author fanzh
     * @date 2019/04/30 16:45
     */
    @Log("开始考试")
    @PostMapping("start")
    public ExecResult<StartExamDto> start(@RequestBody ExaminationRecord examRecord) {
        return ExecResultUtil.success(answerService.start(examRecord.getUserId(), SysUtil.getUser(), examRecord.getExaminationId()));
    }

    /**
     * 开始考试
     *
     * @param examinationId examinationId
     * @param identifier    identifier
     * @return ResponseBean
     * @author fanzh
     * @date 2020/3/21 5:51 下午
     */
    @Log("开始考试(匿名)")
    @PostMapping("anonymousUser/start")
    public ExecResult<StartExamDto> anonymousUserStart(String identifier, Long examinationId) {
        return ExecResultUtil.success(answerService.anonymousUserStart(identifier, examinationId));
    }

    /**
     * 获取服务器当前时间
     *
     * @return ResponseBean
     * @author fanzh
     * @date 2019/05/07 22:03
     */
    @ApiOperation(value = "获取服务器当前时间", notes = "获取服务器当前时间")
    @GetMapping("currentTime")
    public ExecResult<String> currentTime() {
        return ExecResultUtil.success(DateUtils.localDateToString(LocalDateTime.now()));
    }

    /**
     * 完成批改
     *
     * @param examRecord examRecord
     * @return ResponseBean
     * @author fanzh
     * @date 2019/06/19 14:33
     */
    @PutMapping("completeMarking")
    public ExecResult<Boolean> completeMarking(@RequestBody ExaminationRecord examRecord) {
        answerService.completeMarking(examRecord.getId());
        return ExecResultUtil.success(true);
    }

    /**
     * 查询考试监控数据
     *
     * @param tenantCode tenantCode
     * @return ResponseBean
     * @author fanzh
     * @date 2019/10/27 20:07:38
     */
    @GetMapping("dashboard")
    public ExecResult<ExaminationDashboardDto> findExamDashboardData(@RequestParam @NotBlank String tenantCode) {
        return ExecResultUtil.success(examRecordService.findExamDashboardData(tenantCode));
    }

    /**
     * 查询过去n天的考试记录数据
     *
     * @param tenantCode tenantCode
     * @param pastDays   pastDays
     * @return ResponseBean
     * @author fanzh
     * @date 2020/1/31 5:46 下午
     */
    @GetMapping("dashboard/examRecordTendency")
    public ExecResult<ExaminationDashboardDto> findExamRecordTendency(
            @RequestParam @NotBlank String tenantCode
            , @RequestParam @NotBlank Integer pastDays
    ) {
        return ExecResultUtil.success(examRecordService.findExamRecordTendency(tenantCode, pastDays));
    }

    /**
     * 成绩详情
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2020/2/20 23:54
     */
    @ApiImplicitParam(name = "id", value = "考试记录ID", required = true, dataType = "Long", paramType = "path")
    @ApiOperation(value = "成绩详情", notes = "根据考试记录id获取成绩详情")
    @GetMapping("/{id}/details")
    public ExecResult<ExaminationRecordDto> getExaminationRecordInfo(@PathVariable Long id) {
        return ExecResultUtil.success(examRecordService.getExaminationRecordInfo(id));
    }

}
