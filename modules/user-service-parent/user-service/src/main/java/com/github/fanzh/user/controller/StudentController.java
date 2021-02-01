package com.github.fanzh.user.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.entity.PageEntity;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.OptionalUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.common.log.annotation.Log;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.user.api.module.Student;
import com.github.fanzh.user.service.StudentService;
import com.github.fanzh.user.api.dto.StudentDto;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 学生管理Controller
 *
 * @author fanzh
 * @date 2019/07/09 15:29
 */
@Slf4j
@AllArgsConstructor
@Api("学生管理")
@RestController
@RequestMapping("/v1/students")
public class StudentController {

    private final StudentService studentService;

    /**
     * 根据ID获取
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2019/07/09 15:30
     */
    @ApiOperation(value = "获取学生信息", notes = "根据学生id获取学生详细信息")
    @ApiImplicitParam(name = "id", value = "学生ID", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/{id}")
    public ExecResult<Student> student(@PathVariable Long id) {
        return ExecResultUtil.success(studentService.baseGetById(id));
    }

    /**
     * 分页查询
     *
     * @param pageEntity
     * @param student
     * @return
     */
    @GetMapping("studentList")
    @ApiOperation(value = "获取学生列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = CommonConstant.PAGE_NUM, value = "分页页码", defaultValue = CommonConstant.PAGE_NUM_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.PAGE_SIZE, value = "分页大小", defaultValue = CommonConstant.PAGE_SIZE_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.SORT, value = "排序字段", defaultValue = CommonConstant.PAGE_SORT_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.ORDER, value = "排序方向", defaultValue = CommonConstant.PAGE_ORDER_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = "studentDto", value = "学生信息", dataType = "StudentDto")
    })
    public ExecResult<Page<Student>> userList(PageEntity pageEntity, Student student) {
        student.setTenantCode(SysUtil.getTenantCode());
        return ExecResultUtil.success(studentService.baseListOrPage(OptionalUtil.build(pageEntity), student));
    }

    /**
     * 创建学生
     *
     * @param studentDto studentDto
     * @return ResponseBean
     * @author fanzh
     * @date 2019/07/09 15:31
     */
    @PostMapping
    @ApiOperation(value = "新增学生", notes = "新增学生")
    @ApiImplicitParam(name = "studentDto", value = "学生实体student", required = true, dataType = "StudentDto")
    @Log("新增学生")
    public ExecResult<Boolean> add(@RequestBody @Valid StudentDto studentDto) {
        studentService.addStudent(studentDto);
        return ExecResultUtil.success(true);
    }

    /**
     * 更新学生
     *
     * @param studentDto studentDto
     * @return ResponseBean
     * @author fanzh
     * @date 2019/07/09 15:32
     */
    @ApiImplicitParam(name = "studentDto", value = "学生实体student", required = true, dataType = "StudentDto")
    @ApiOperation(value = "更新学生信息", notes = "根据学生id更新学生的基本信息")
    @PutMapping
    public ExecResult<Boolean> update(@RequestBody @Valid StudentDto studentDto) {
        Student student = new Student();
        BeanUtils.copyProperties(studentDto, student);
        studentService.baseUpdate(student);
        return ExecResultUtil.success(true);
    }

    /**
     * 删除学生
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2019/07/09 15:33
     */
    @ApiOperation(value = "删除学生", notes = "根据ID删除学生")
    @ApiImplicitParam(name = "id", value = "学生ID", required = true, paramType = "path")
    @DeleteMapping("/{id}")
    public ExecResult<Boolean> delete(@PathVariable Long id) {
        studentService.baseLogicDelete(id);
        return ExecResultUtil.success(true);
    }

    /**
     * 批量删除
     *
     * @param ids ids
     * @return ResponseBean
     * @author fanzh
     * @date 2019/07/09 15:34
     */
    @ApiImplicitParam(name = "ids", value = "学生ID", dataType = "Long")
    @ApiOperation(value = "批量删除学生", notes = "根据学生id批量删除学生")
    @PostMapping("deleteAll")
    public ExecResult<Boolean> deleteAll(@RequestBody List<Long> ids) {
        studentService.baseLogicDelete(SetUtil.build(ids));
        return ExecResultUtil.success(true);
    }

    /**
     * 根据ID查询
     *
     * @param ids ids
     * @return ResponseBean
     * @author fanzh
     * @date 2019/07/09 15:34
     */
    @ApiImplicitParam(name = "ids", value = "学生ID", required = true, paramType = "Long")
    @ApiOperation(value = "根据ID查询学生", notes = "根据ID查询学生")
    @RequestMapping(value = "findById", method = RequestMethod.POST)
    public ExecResult<List<Student>> findById(@RequestBody List<Long> ids) {
        List<Student> studentList = studentService.baseFindById(SetUtil.build(ids));
        return ExecResultUtil.success(studentList);
    }
}
