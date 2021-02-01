package com.github.fanzh.exam.controller;


import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.log.annotation.Log;
import com.github.fanzh.common.security.annotations.AdminTenantTeacherAuthorization;
import com.github.fanzh.exam.api.constants.ExamSubjectConstant;
import com.github.fanzh.exam.api.dto.SubjectCategoryDto;
import com.github.fanzh.exam.api.module.SubjectCategory;
import com.github.fanzh.exam.service.SubjectCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
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
 * 题目分类controller
 *
 * @author fanzh
 * @date 2018/12/4 21:57
 */
@AllArgsConstructor
@Api("题库分类信息管理")
@RestController
@RequestMapping("/v1/subjectCategory")
public class SubjectCategoryController {

    private final SubjectCategoryService categoryService;

    /**
     * 返回树形分类集合
     *
     * @return List
     * @author fanzh
     * @date 2018/12/04 22:03
     */
    @GetMapping(value = "categories")
    @ApiOperation(value = "获取分类列表")
    public List<SubjectCategoryDto> menus() {
        return categoryService.menus();
    }

    /**
     * 根据ID获取
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2018/12/04 21:59
     */
    @ApiImplicitParam(name = "id", value = "分类ID", required = true, dataType = "Long", paramType = "path")
    @ApiOperation(value = "获取分类信息", notes = "根据分类id获取分类详细信息")
    @GetMapping("/{id}")
    public ExecResult<SubjectCategory> subjectCategory(@PathVariable Long id) {
        return new ExecResult<>(categoryService.baseGetById(id));
    }

    /**
     * 新增分类
     *
     * @param subjectCategory subjectCategory
     * @return ResponseBean
     * @author fanzh
     * @date 2018/12/04 22:00
     */
    @Log("新增题目分类")
    @ApiImplicitParam(name = "subjectCategory", value = "分类实体subjectCategory", required = true, dataType = "SubjectCategory")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "创建分类", notes = "创建分类")
    @PostMapping
    public ExecResult<Boolean> addSubjectCategory(@RequestBody @Valid SubjectCategory subjectCategory) {
        subjectCategory.setType(ExamSubjectConstant.PUBLIC_CATEGORY);
        categoryService.baseSave(subjectCategory);
        return ExecResultUtil.success(true);
    }

    /**
     * 更新分类
     *
     * @param subjectCategory subjectCategory
     * @return ResponseBean
     * @author fanzh
     * @date 2018/12/04 22:01
     */
    @Log("更新题目分类")
    @ApiImplicitParam(name = "subjectCategory", value = "分类实体subjectCategory", required = true, dataType = "SubjectCategory")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "更新分类信息", notes = "根据分类id更新分类的基本信息")
    @PutMapping
    public ExecResult<Boolean> updateSubjectCategory(@RequestBody @Valid SubjectCategory subjectCategory) {
        categoryService.baseUpdate(subjectCategory);
        return ExecResultUtil.success(true);
    }

    /**
     * 根据ID删除
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2018/12/04 22:02
     */
    @Log("删除题目分类")
    @ApiImplicitParam(name = "id", value = "分类ID", required = true, paramType = "path")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "删除分类", notes = "根据ID删除分类")
    @DeleteMapping("/{id}")
    public ExecResult<Boolean> deleteSubjectCategory(@PathVariable Long id) {
        categoryService.baseLogicDelete(id);
        return ExecResultUtil.success(true);
    }
}
