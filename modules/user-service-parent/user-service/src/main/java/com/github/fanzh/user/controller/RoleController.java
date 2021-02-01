package com.github.fanzh.user.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.entity.PageEntity;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.OptionalUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.security.annotations.AdminTenantTeacherAuthorization;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.user.api.module.Role;
import com.github.fanzh.user.service.RoleMenuService;
import com.github.fanzh.user.service.RoleService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 角色controller
 *
 * @author fanzh
 * @date 2018/8/26 22:50
 */
@Slf4j
@AllArgsConstructor
@Api("角色信息管理")
@RestController
@RequestMapping("/v1/role")
public class RoleController {

    private final RoleService roleService;

    private final RoleMenuService roleMenuService;

    /**
     * 根据id获取角色
     *
     * @param id id
     * @return RoleVo
     * @author fanzh
     * @date 2018/9/14 18:20
     */
    @ApiImplicitParam(name = "id", value = "角色ID", required = true, dataType = "Long", paramType = "path")
    @ApiOperation(value = "获取角色信息", notes = "根据角色id获取角色详细信息")
    @GetMapping("/{id}")
    public Role role(@PathVariable Long id) {
        return roleService.baseGetById(id);
    }

    /**
     * 角色分页查询
     *
     * @param pageNum  pageNum
     * @param pageSize pageSize
     * @param sort     sort
     * @param order    order
     * @param role     role
     * @return PageInfo
     * @author fanzh
     * @date 2018/10/24 22:13
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = CommonConstant.PAGE_NUM, value = "分页页码", defaultValue = CommonConstant.PAGE_NUM_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.PAGE_SIZE, value = "分页大小", defaultValue = CommonConstant.PAGE_SIZE_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.SORT, value = "排序字段", defaultValue = CommonConstant.PAGE_SORT_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.ORDER, value = "排序方向", defaultValue = CommonConstant.PAGE_ORDER_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = "role", value = "角色信息", dataType = "RoleVo")
    })
    @ApiOperation(value = "获取角色列表")
    @GetMapping("roleList")
    public ExecResult<Page<Role>> roleList(PageEntity pageEntity, Role role) {
        role.setTenantCode(SysUtil.getTenantCode());
        return ExecResultUtil.success(roleService.baseListOrPage(OptionalUtil.build(pageEntity), role));
    }

    /**
     * 查询所有角色
     *
     * @param role role
     * @return ResponseBean
     * @author fanzh
     * @date 2019/05/15 23:29
     */
    @ApiImplicitParam(name = "role", value = "角色信息", dataType = "RoleVo")
    @ApiOperation(value = "获取全部角色列表")
    @GetMapping("allRoles")
    public ExecResult<List<Role>> allRoles(Role role) {
        if (ParamsUtil.isEmpty(role.getDelFlag())) {
            role.setDelFlag(CommonConstant.DEL_FLAG_NORMAL);
        }
        role.setTenantCode(SysUtil.getTenantCode());
        role.setApplicationCode(SysUtil.getSysCode());
        return ExecResultUtil.success(roleService.baseList(role));
    }

    /**
     * 修改角色
     *
     * @param role role
     * @return ResponseBean
     * @author fanzh
     * @date 2018/9/14 18:22
     */
    @ApiImplicitParam(name = "role", value = "角色实体role", required = true, dataType = "RoleVo")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "更新角色信息", notes = "根据角色id更新角色的基本信息")
    @PutMapping
    public ExecResult<Boolean> updateRole(@RequestBody @Valid Role role) {
        roleService.baseUpdate(role);
        return ExecResultUtil.success(true);
    }

    /**
     * 更新角色菜单
     *
     * @param role role
     * @return ResponseBean
     * @author fanzh
     * @date 2018/10/28 14:20
     */
    @ApiImplicitParam(name = "role", value = "角色实体role", required = true, dataType = "RoleVo")
    @ApiOperation(value = "更新角色菜单信息", notes = "更新角色菜单信息")
    @PutMapping("roleMenuUpdate")
    public ExecResult<Boolean> updateRoleMenu(@RequestBody Role role) {
        String menuIds = role.getMenuIds();
        if (ParamsUtil.isEmpty(menuIds)) {
            return ExecResultUtil.success(false, "Param menuIds cannot be empty");
        }
        Optional<Role> roleOpt = roleService.baseFindById(role.getId());
        if (!roleOpt.isPresent()) {
            return ExecResultUtil.success(false, "Param roleId is invalid");
        }
        roleMenuService.put(role.getId(), Stream.of(menuIds.split(",")).map(Long::parseLong).collect(Collectors.toList()));
        return ExecResultUtil.success(true);
    }

    /**
     * 创建角色
     *
     * @param role role
     * @return ResponseBean
     * @author fanzh
     * @date 2018/9/14 18:23
     */
    @ApiImplicitParam(name = "role", value = "角色实体role", required = true, dataType = "RoleVo")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "创建角色", notes = "创建角色")
    @PostMapping
    public ExecResult<Boolean> role(@RequestBody @Valid Role role) {
        roleService.baseSave(role);
        return ExecResultUtil.success(true);
    }

    /**
     * 根据id删除角色
     *
     * @param id id
     * @return RoleVo
     * @author fanzh
     * @date 2018/9/14 18:24
     */
    @ApiImplicitParam(name = "id", value = "角色ID", required = true, paramType = "path")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "删除角色", notes = "根据ID删除角色")
    @DeleteMapping("/{id}")
    public ExecResult<Boolean> deleteRole(@PathVariable Long id) {
        roleService.delete(Arrays.asList(id));
        return ExecResultUtil.success(true);
    }

    /**
     * 批量删除
     *
     * @param ids ids
     * @return ResponseBean
     * @author fanzh
     * @date 2018/12/4 10:00
     */
    @ApiImplicitParam(name = "ids", value = "角色ID", dataType = "Long")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "批量删除角色", notes = "根据角色id批量删除角色")
    @PostMapping("deleteAll")
    public ExecResult<Boolean> deleteAllRoles(@RequestBody List<Long> ids) {
        roleService.delete(ids);
        return new ExecResult<>(true);
    }
}
