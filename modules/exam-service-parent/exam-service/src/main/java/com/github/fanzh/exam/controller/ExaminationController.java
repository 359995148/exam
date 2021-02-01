package com.github.fanzh.exam.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.entity.BaseEntity;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.entity.PageEntity;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.OptionalUtil;
import com.github.fanzh.common.basic.utils.PageUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.common.log.annotation.Log;
import com.github.fanzh.common.security.annotations.AdminTenantTeacherAuthorization;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.exam.api.dto.ExaminationDto;
import com.github.fanzh.exam.api.dto.SubjectDto;
import com.github.fanzh.exam.api.module.Course;
import com.github.fanzh.exam.api.module.Examination;
import com.github.fanzh.exam.api.module.ExaminationSubject;
import com.github.fanzh.exam.service.CourseService;
import com.github.fanzh.exam.service.ExaminationService;
import com.github.fanzh.exam.service.ExaminationSubjectService;
import com.github.fanzh.exam.service.SubjectService;
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

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 考试controller
 *
 * @author fanzh
 * @date 2018/11/8 21:26
 */
@Slf4j
@AllArgsConstructor
@Api("考试信息管理")
@RestController
@RequestMapping("/v1/examination")
public class ExaminationController {

    private final ExaminationService examinationService;
    private final CourseService courseService;
    private final ExaminationSubjectService examinationSubjectService;
    private final SubjectService subjectService;

    /**
     * 根据ID获取
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:08
     */
    @ApiImplicitParam(name = "id", value = "考试ID", required = true, dataType = "String", paramType = "path")
    @ApiOperation(value = "获取考试信息", notes = "根据考试id获取考试详细信息")
    @GetMapping("/{id}")
    public ExecResult<Examination> examination(@PathVariable Long id) {
        return ExecResultUtil.success(examinationService.baseGetById(id));

    }

    /**
     * 根据ID获取
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:08
     */
    @GetMapping("/anonymousUser/{id}")
    @ApiOperation(value = "获取考试信息", notes = "根据考试id获取考试详细信息")
    @ApiImplicitParam(name = "id", value = "考试ID", required = true, dataType = "String", paramType = "path")
    public ExecResult<Examination> anonymousUserGet(@PathVariable Long id) {
        return ExecResultUtil.success(examinationService.baseGetById(id));
    }

    /**
     * 获取分页数据
     *
     * @param pageEntity
     * @param examination
     * @return
     */
    @GetMapping("examinationList")
    @ApiOperation(value = "获取考试列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = CommonConstant.PAGE_NUM, value = "分页页码", defaultValue = CommonConstant.PAGE_NUM_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.PAGE_SIZE, value = "分页大小", defaultValue = CommonConstant.PAGE_SIZE_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.SORT, value = "排序字段", defaultValue = CommonConstant.PAGE_SORT_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.ORDER, value = "排序方向", defaultValue = CommonConstant.PAGE_ORDER_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = "examination", value = "考试信息", dataType = "Examination")
    })
    public ExecResult<Page<ExaminationDto>> examinationList(PageEntity pageEntity, Examination examination) {
        Page<Examination> pageSource = examinationService.baseListOrPage(OptionalUtil.build(pageEntity), examination);
        if (ParamsUtil.isEmpty(pageSource.getRecords())) {
            return ExecResultUtil.success(PageUtil.copy(pageSource));
        }
        List<Course> courses = courseService.baseFindById(pageSource.getRecords().stream().map(o -> o.getCourseId()).collect(Collectors.toSet()));
        List<ExaminationDto> examinationDtos = pageSource.getRecords().stream().map(exam -> {
            ExaminationDto examinationDto = new ExaminationDto();
            BeanUtils.copyProperties(exam, examinationDto);
            // 设置考试所属课程
            courses.stream().filter(tempCourse -> tempCourse.getId().equals(exam.getCourseId())).findAny().ifPresent(examinationDto::setCourse);
            // 初始化封面图片
            examinationService.initExaminationLogo(examinationDto);
            return examinationDto;
        }).collect(Collectors.toList());
        Page<ExaminationDto> page = PageUtil.copy(pageSource);
        page.setRecords(examinationDtos);
        return ExecResultUtil.success(page);
    }

    /**
     * 根据考试ID获取题目分页数据
     *
     * @param pageEntity
     * @param subjectDto
     * @return
     */
    @RequestMapping("subjectList")
    @ApiOperation(value = "获取题目列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = CommonConstant.PAGE_NUM, value = "分页页码", defaultValue = CommonConstant.PAGE_NUM_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.PAGE_SIZE, value = "分页大小", defaultValue = CommonConstant.PAGE_SIZE_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.SORT, value = "排序字段", defaultValue = CommonConstant.PAGE_SORT_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.ORDER, value = "排序方向", defaultValue = CommonConstant.PAGE_ORDER_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = "subjectDto", value = "题目信息", dataType = "SubjectDto")
    })
    public ExecResult<Page<SubjectDto>> subjectList(PageEntity pageEntity, SubjectDto subjectDto) {
        ExaminationSubject examinationSubject = new ExaminationSubject();
        examinationSubject.setTenantCode(SysUtil.getTenantCode());
        examinationSubject.setExaminationId(subjectDto.getExaminationId());
        examinationSubject.setCategoryId(subjectDto.getCategoryId());
        Page<ExaminationSubject> pageSource = examinationSubjectService.baseListOrPage(OptionalUtil.build(pageEntity), examinationSubject);
        if (ParamsUtil.isEmpty(pageSource.getRecords())) {
            return ExecResultUtil.success(PageUtil.copy(pageSource));
        }
        List<SubjectDto> subjectDtoList = subjectService.findSubjectDtoList(pageSource.getRecords());
        Page<SubjectDto> page = PageUtil.copy(pageSource);
        page.setRecords(subjectDtoList);
        return ExecResultUtil.success(page);
    }

    /**
     * 获取全部题目
     *
     * @param subjectDto subjectDto
     * @return ResponseBean
     * @author fanzh
     * @date 2020/3/12 1:00 下午
     */
    @ApiOperation(value = "获取全部题目列表")
    @ApiImplicitParam(name = "subjectDto", value = "题目信息", dataType = "SubjectDto")
    @GetMapping("anonymousUser/allSubjectList")
    public ExecResult<List<SubjectDto>> allSubjectList(SubjectDto subjectDto) {
        List<ExaminationSubject> examinationSubjectList = examinationSubjectService.findByExaminationId(subjectDto.getExaminationId());
        if (ParamsUtil.isEmpty(examinationSubjectList)) {
            return ExecResultUtil.success(Collections.emptyList());
        }
        return ExecResultUtil.success(subjectService.findSubjectDtoList(examinationSubjectList, true, false));
    }

    /**
     * 创建
     *
     * @param examinationDto examinationDto
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:14
     */
    @Log("新增考试")
    @ApiImplicitParam(name = "examinationDto", value = "考试实体examinationDto", required = true, dataType = "ExaminationDto")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "创建考试", notes = "创建考试")
    @PostMapping
    public ExecResult<Boolean> addExamination(@RequestBody @Valid ExaminationDto examinationDto) {
        examinationService.initExaminationLogo(examinationDto);
        Examination examination = new Examination();
        BeanUtils.copyProperties(examinationDto, examination);
        examination.setCourseId(examinationDto.getCourse().getId());
        examinationService.baseSave(examinationDto);
        return ExecResultUtil.success(true);
    }

    /**
     * 更新
     *
     * @param examinationDto examinationDto
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:15
     */
    @Log("更新考试")
    @ApiImplicitParam(name = "examinationDto", value = "考试实体answer", required = true, dataType = "ExaminationDto")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "更新考试信息", notes = "根据考试id更新考试的基本信息")
    @PutMapping
    public ExecResult<Boolean> updateExamination(@RequestBody @Valid ExaminationDto examinationDto) {
        if (examinationDto.getAvatarId() == null || examinationDto.getAvatarId() == 0L) {
            examinationService.initExaminationLogo(examinationDto);
        }
        Examination examination = new Examination();
        BeanUtils.copyProperties(examinationDto, examination);
        if (ParamsUtil.isNotEmpty(examinationDto.getCourse())) {
            examination.setCourseId(examinationDto.getCourse().getId());
        }
        examinationService.baseUpdate(examination);
        return ExecResultUtil.success(true);
    }

    /**
     * 删除考试
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:20
     */
    @Log("删除考试")
    @ApiImplicitParam(name = "id", value = "考试ID", required = true, paramType = "path")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "删除考试", notes = "根据ID删除考试")
    @DeleteMapping("{id}")
    public ExecResult<Boolean> deleteExamination(@PathVariable Long id) {
        examinationService.deleteExamination(SetUtil.build(id));
        return ExecResultUtil.success(true);
    }

    /**
     * 批量删除
     *
     * @param ids ids
     * @return ResponseBean
     * @author fanzh
     * @date 2018/12/03 22:03
     */
    @Log("批量删除考试")
    @AdminTenantTeacherAuthorization
    @ApiImplicitParam(name = "ids", value = "考试ID", dataType = "Long")
    @ApiOperation(value = "批量删除考试", notes = "根据考试id批量删除考试")
    @PostMapping("deleteAll")
    public ExecResult<Boolean> deleteAllExaminations(@RequestBody List<Long> ids) {
        examinationService.deleteExamination(SetUtil.build(ids));
        return ExecResultUtil.success(true);
    }

    /**
     * 根据考试ID查询题目id列表
     *
     * @param examinationId examinationId
     * @return ResponseBean
     * @author fanzh
     * @date 2019/06/18 14:31
     */
    @ApiImplicitParam(name = "examinationId", value = "考试ID", required = true, paramType = "path")
    @GetMapping("/{examinationId}/subjectIds")
    public ExecResult<List<ExaminationSubject>> findExaminationSubjectIds(@PathVariable Long examinationId) {
        List<ExaminationSubject> subjects = examinationSubjectService.findByExaminationId(examinationId);
        subjects.forEach(BaseEntity::clearCommonValue);
        return ExecResultUtil.success(subjects);
    }

    /**
     * 根据考试ID查询题目id列表
     *
     * @param examinationId examinationId
     * @return ResponseBean
     * @author fanzh
     * @date 2019/06/18 14:31
     */
    @ApiImplicitParam(name = "examinationId", value = "考试ID", required = true, paramType = "path")
    @GetMapping("/anonymousUser/{examinationId}/subjectIds")
    public ExecResult<List<ExaminationSubject>> anonymousUserFindExaminationSubjectIds(@PathVariable Long examinationId) {
        List<ExaminationSubject> subjects = examinationSubjectService.findByExaminationId(examinationId);
        subjects.forEach(BaseEntity::clearCommonValue);
        return ExecResultUtil.success(subjects);
    }

    /**
     * 根据考试ID生成二维码
     *
     * @param examinationId examinationId
     * @param response      response
     * @author fanzh
     * @date 2020/3/15 1:16 下午
     */
    @ApiOperation(value = "生成二维码", notes = "生成二维码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "examinationId", value = "考试ID", required = true, dataType = "Long", paramType = "path")
    })
    @GetMapping("anonymousUser/generateQrCode/{examinationId}")
    public void produceCode(@PathVariable Long examinationId, HttpServletResponse response) throws java.lang.Exception {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(examinationService.produceCode(examinationId)); ServletOutputStream out = response.getOutputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            ImageIO.write(image, "PNG", out);
        }
    }

    /**
     * 根据考试ID生成二维码
     *
     * @param examinationId examinationId
     * @param response      response
     * @author fanzh
     * @date 2020/3/21 5:38 下午
     */
    @ApiOperation(value = "生成二维码(v2)", notes = "生成二维码(v2)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "examinationId", value = "考试ID", required = true, dataType = "Long", paramType = "path")
    })
    @GetMapping("anonymousUser/generateQrCode/v2/{examinationId}")
    public void produceCodeV2(@PathVariable Long examinationId, HttpServletResponse response) throws java.lang.Exception {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(examinationService.produceCodeV2(examinationId)); ServletOutputStream out = response.getOutputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            ImageIO.write(image, "PNG", out);
        }
    }
}
