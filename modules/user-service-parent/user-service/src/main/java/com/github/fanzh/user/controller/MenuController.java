package com.github.fanzh.user.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.common.core.utils.excel.ExcelToolUtil;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.constant.SqlField;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.entity.PageEntity;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.JsonUtil;
import com.github.fanzh.common.core.utils.OptionalUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.security.annotations.AdminTenantTeacherAuthorization;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.user.api.dto.MenuDto;
import com.github.fanzh.user.api.module.Menu;
import com.github.fanzh.user.excel.listener.MenuImportListener;
import com.github.fanzh.user.excel.model.MenuExcelModel;
import com.github.fanzh.user.service.MenuService;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 菜单controller
 *
 * @author fanzh
 * @date 2018/8/26 22:48
 */
@Slf4j
@AllArgsConstructor
@Api("菜单信息管理")
@RestController
@RequestMapping("/v1/menu")
public class MenuController {

    private final MenuService menuService;

    /**
     * 返回当前用户的树形菜单集合
     *
     * @return 当前用户的树形菜单
     */
    @ApiOperation(value = "获取当前用户的菜单列表")
    @GetMapping(value = "userMenu")
    public List<MenuDto> userMenu() {
        return menuService.findUserMenu();
    }

    /**
     * 返回树形菜单集合
     *
     * @return 树形菜单集合
     */
    @GetMapping(value = "menus")
    @ApiOperation(value = "获取树形菜单列表")
    public List<MenuDto> treeMenus() {
        return menuService.treeMenus();
    }

    /**
     * 新增菜单
     *
     * @param menu menu
     * @return ResponseBean
     * @author fanzh
     * @date 2018/8/27 16:12
     */
    @ApiImplicitParam(name = "menu", value = "角色实体menu", required = true, dataType = "Menu")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "菜单新增", notes = "菜单新增")
    @PostMapping
    public ExecResult<Boolean> addMenu(@RequestBody @Valid Menu menu) {
        menuService.baseSave(menu);
        return ExecResultUtil.success(true);
    }

    /**
     * 更新菜单
     *
     * @param menu menu
     * @return ResponseBean
     * @author fanzh
     * @date 2018/10/24 16:34
     */
    @ApiImplicitParam(name = "menu", value = "角色实体menu", required = true, dataType = "Menu")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "更新菜单信息", notes = "根据菜单id更新菜单的基本信息")
    @PutMapping
    public ExecResult<Boolean> updateMenu(@RequestBody @Valid Menu menu) {
        menuService.baseUpdate(menu);
        return ExecResultUtil.success(true);
    }

    /**
     * 根据id删除
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2018/8/27 16:19
     */
    @ApiImplicitParam(name = "id", value = "菜单ID", required = true, paramType = "path")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "删除菜单", notes = "根据ID删除菜单")
    @DeleteMapping("/{id}")
    public ExecResult<Boolean> deleteMenu(@PathVariable Long id) {
        menuService.baseLogicDelete(id);
        return ExecResultUtil.success(true);
    }

    /**
     * 根据id查询菜单
     *
     * @param id
     * @return Menu
     * @author fanzh
     * @date 2018/8/27 16:11
     */
    @ApiImplicitParam(name = "id", value = "菜单ID", required = true, dataType = "Long", paramType = "path")
    @ApiOperation(value = "获取菜单信息", notes = "根据菜单id获取菜单详细信息")
    @GetMapping("/{id}")
    public Menu menu(@PathVariable Long id) {
        return menuService.baseGetById(id);
    }

    /**
     * 获取菜单分页列表
     *
     * @param pageNum  pageNum
     * @param pageSize pageSize
     * @param sort     sort
     * @param order    order
     * @param menu     menu
     * @return PageInfo
     * @author fanzh
     * @date 2018/8/26 23:17
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = CommonConstant.PAGE_NUM, value = "分页页码", defaultValue = CommonConstant.PAGE_NUM_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.PAGE_SIZE, value = "分页大小", defaultValue = CommonConstant.PAGE_SIZE_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.SORT, value = "排序字段", defaultValue = CommonConstant.PAGE_SORT_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.ORDER, value = "排序方向", defaultValue = CommonConstant.PAGE_ORDER_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = "Menu", value = "菜单信息", dataType = "Menu")})
    @ApiOperation(value = "获取菜单列表")
    @GetMapping("menuList")
    public ExecResult<Page<Menu>> menuList(PageEntity pageEntity, Menu menu) {
        // 租户标识过滤条件
        menu.setTenantCode(SysUtil.getTenantCode());
        return ExecResultUtil.success(menuService.baseListOrPage(OptionalUtil.build(pageEntity), menu));
    }

    /**
     * 根据角色查找菜单
     *
     * @param role       角色标识
     * @param tenantCode 租户标识
     * @return ResponseBean
     * @author fanzh
     * @date 2018/8/27 15:58
     */
    @ApiImplicitParam(name = "role", value = "角色名称", required = true, dataType = "String", paramType = "path")
    @ApiOperation(value = "根据角色查找菜单", notes = "根据角色id获取角色菜单")
    @GetMapping("anonymousUser/findMenuByRole/{role}")
    public ExecResult<List<Menu>> findMenuByRole(@PathVariable String role, @RequestParam @NotBlank String tenantCode) {
        return ExecResultUtil.success(menuService.findMenuByRole(role, tenantCode));
    }

    /**
     * 根据角色查找菜单
     *
     * @param roleCode 角色code
     * @return 属性集合
     */
    @ApiImplicitParam(name = "roleCode", value = "角色code", required = true, dataType = "String", paramType = "path")
    @ApiOperation(value = "根据角色查找菜单", notes = "根据角色code获取角色菜单")
    @GetMapping("roleTree/{roleCode}")
    public List<String> roleTree(@PathVariable String roleCode) {
        // 根据角色查找菜单
        List<Menu> menuList = menuService.findMenuByRole(roleCode, SysUtil.getTenantCode());
        // 获取菜单ID
        return menuList.stream().map(menu -> menu.getId().toString()).collect(Collectors.toList());
    }

    /**
     * 查询所有菜单
     *
     * @param tenantCode 租户标识
     * @return ResponseBean
     * @author fanzh
     * @date 2019/04/26 11:50
     */
    @ApiOperation(value = "查询所有菜单", notes = "查询所有菜单")
    @GetMapping("anonymousUser/findAllMenu")
    public ExecResult<List<Menu>> findAllMenu(@RequestParam @NotBlank String tenantCode) {
        return ExecResultUtil.success(menuService.find(
                OptionalUtil.build(tenantCode)
                , OptionalUtil.build(SysUtil.getSysCode())
                , Optional.empty()
        ));
    }

    /**
     * 导出菜单
     *
     * @param ids ids
     * @author fanzh
     * @date 2018/11/28 12:46
     */
    @ApiImplicitParam(name = "ids", value = "菜单ID", required = true, dataType = "Long")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "导出菜单", notes = "根据菜单id导出菜单")
    @PostMapping("export")
    public void exportMenu(@RequestBody List<Long> ids, HttpServletRequest request, HttpServletResponse response) {
        EntityWrapper<Menu> ew = new EntityWrapper<>();
        ew.eq(SqlField.DEL_FLAG, CommonConstant.DEL_FLAG_NORMAL);
        if (ParamsUtil.isNotEmpty(ids)) {
            ew.in(SqlField.ID, ids);
        } else {
            ew.eq(SqlField.TENANT_CODE, SysUtil.getTenantCode());
        }
        // 导出当前租户下的所有菜单
        List<Menu> menus = menuService.selectList(ew);
        List<MenuExcelModel> menuExcelModelList = JsonUtil.listToList(menus, MenuExcelModel.class);
        try {
            ExcelToolUtil.exportExcel(request, response, menuExcelModelList, MenuExcelModel.class);
        } catch (Exception e) {
            log.error("Export menu data failed: {}", e.getMessage());
        }
    }

    /**
     * 导入数据
     *
     * @param file file
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/28 12:51
     */
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "导入菜单", notes = "导入菜单")
    @PostMapping("import")
    public ExecResult<Boolean> importMenu(@ApiParam(value = "要上传的文件", required = true) MultipartFile file) {
        return ExecResultUtil.success(ExcelToolUtil.importExcel(file, MenuExcelModel.class, new MenuImportListener(menuService)));
    }
}
