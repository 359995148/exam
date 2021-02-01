package com.github.fanzh.exam.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.entity.PageEntity;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.OptionalUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.common.log.annotation.Log;
import com.github.fanzh.common.security.annotations.AdminTenantTeacherAuthorization;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.exam.api.module.Course;
import com.github.fanzh.exam.service.CourseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 课程controller
 *
 * @author fanzh
 * @date 2018/11/8 21:25
 */
@Slf4j
@AllArgsConstructor
@Api("课程信息管理")
@RestController
@RequestMapping("/v1/course")
public class CourseController {

    private final CourseService courseService;

    /**
     * 获取分页数据
     *
     * @param course course
     * @return PageInfo
     * @author fanzh
     * @date 2018/11/10 21:30
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = CommonConstant.PAGE_NUM, value = "分页页码", defaultValue = CommonConstant.PAGE_NUM_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.PAGE_SIZE, value = "分页大小", defaultValue = CommonConstant.PAGE_SIZE_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.SORT, value = "排序字段", defaultValue = CommonConstant.PAGE_SORT_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.ORDER, value = "排序方向", defaultValue = CommonConstant.PAGE_ORDER_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = "course", value = "课程信息", dataType = "Course")
    })
    @ApiOperation(value = "获取课程列表")
    @GetMapping("courseList")
    public ExecResult<Page<Course>> courseList(PageEntity pageEntity, Course course) {
        course.setTenantCode(SysUtil.getTenantCode());
        return ExecResultUtil.success(courseService.baseListOrPage(OptionalUtil.build(pageEntity), course));
    }

    /**
     * 根据ID获取
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:28
     */
    @ApiImplicitParam(name = "id", value = "课程ID", required = true, dataType = "Long", paramType = "path")
    @ApiOperation(value = "获取课程信息", notes = "根据课程id获取课程详细信息")
    @GetMapping("/{id}")
    public ExecResult<Course> get(@PathVariable Long id) {
        return ExecResultUtil.success(courseService.baseGetById(id));
    }

    /**
     * 创建
     *
     * @param course course
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:31
     */
    @Log("新增课程")
    @ApiImplicitParam(name = "course", value = "课程实体course", required = true, dataType = "Course")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "创建课程", notes = "创建课程")
    @PostMapping
    public ExecResult<Boolean> addCourse(@RequestBody @Valid Course course) {
        courseService.baseSave(course);
        return ExecResultUtil.success(true);
    }

    /**
     * 更新
     *
     * @param course course
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:31
     */
    @Log("更新课程")
    @ApiImplicitParam(name = "course", value = "课程实体course", required = true, dataType = "Course")
    @ApiOperation(value = "更新课程信息", notes = "根据课程id更新课程的基本信息")
    @AdminTenantTeacherAuthorization
    @PutMapping
    public ExecResult<Boolean> updateCourse(@RequestBody @Valid Course course) {
        courseService.baseUpdate(course);
        return ExecResultUtil.success(true);
    }

    /**
     * 删除
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/10 21:32
     */
    @Log("删除课程")
    @ApiImplicitParam(name = "id", value = "课程ID", required = true, paramType = "path")
    @ApiOperation(value = "删除课程", notes = "根据ID删除课程")
    @AdminTenantTeacherAuthorization
    @DeleteMapping("{id}")
    public ExecResult<Boolean> deleteCourse(@PathVariable Long id) {
        courseService.baseLogicDelete(id);
        return ExecResultUtil.success(true);
    }

    /**
     * 批量删除
     *
     * @param ids ids
     * @return ResponseBean
     * @author fanzh
     * @date 2018/12/4 11:26
     */
    @Log("批量删除课程")
    @ApiImplicitParam(name = "ids", value = "课程ID", dataType = "Long")
    @ApiOperation(value = "批量删除课程", notes = "根据课程id批量删除课程")
    @AdminTenantTeacherAuthorization
    @PostMapping("deleteAll")
    public ExecResult<Boolean> deleteAllCourses(@RequestBody List<Long> ids) {
        courseService.baseLogicDelete(SetUtil.build(ids));
        return ExecResultUtil.success(true);
    }
}
