package com.github.fanzh.user.controller;


import com.github.fanzh.common.basic.vo.DeptVo;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.JsonUtil;
import com.github.fanzh.common.security.annotations.AdminTenantTeacherAuthorization;
import com.github.fanzh.user.api.module.Dept;
import com.github.fanzh.user.service.DeptService;
import com.github.fanzh.user.api.dto.DeptDto;
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
 * 部门controller
 *
 * @author fanzh
 * @date 2018/8/26 0026 22:49
 */
@AllArgsConstructor
@Api("部门信息管理")
@RestController
@RequestMapping("/v1/dept")
public class DeptController {

    private final DeptService deptService;

    @ApiOperation(value = "部门树形列表查询")
    @GetMapping(value = "depts")
    public List<DeptDto> treeDept() {
        return deptService.treeDept();
    }

    @ApiImplicitParam(name = "id", value = "部门ID", required = true, dataType = "Long", paramType = "path")
    @ApiOperation(value = "获取部门信息", notes = "根据部门id获取部门详细信息")
    @GetMapping("/{id}")
    public Dept get(@PathVariable Long id) {
        return deptService.baseGetById(id);
    }

    @ApiImplicitParam(name = "dept", value = "部门实体", required = true, dataType = "Dept")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "部门新增")
    @PostMapping
    public ExecResult<Boolean> add(@RequestBody @Valid Dept dept) {
        deptService.baseSave(dept);
        return ExecResultUtil.success(true);
    }

    @ApiImplicitParam(name = "id", value = "部门ID", required = true, paramType = "path")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "删除部门")
    @DeleteMapping("/{id}")
    public ExecResult<Boolean> delete(@PathVariable Long id) {
        deptService.baseLogicDelete(id);
        return ExecResultUtil.success(true);
    }

    @ApiImplicitParam(name = "dept", value = "部门实体", required = true, dataType = "Dept")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "更新部门信息")
    @PutMapping
    public ExecResult<Boolean> update(@RequestBody @Valid Dept dept) {
        deptService.baseUpdate(dept);
        return ExecResultUtil.success(true);
    }

    @ApiImplicitParam(name = "ids", value = "部门ID", required = true, dataType = "Long")
    @ApiOperation(value = "批量查询部门信息", notes = "根据Ids批量查询信息")
    @PostMapping(value = "findById")
    public ExecResult<List<DeptVo>> findById(@RequestBody List<Long> ids) {
        List<Dept> list = deptService.selectBatchIds(ids);
        return ExecResultUtil.success(JsonUtil.listToList(list, DeptVo.class));
    }
}
