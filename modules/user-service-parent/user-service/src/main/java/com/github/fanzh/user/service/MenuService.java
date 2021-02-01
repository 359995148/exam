package com.github.fanzh.user.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.basic.utils.EntityWrapperUtil;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.constant.SqlField;
import com.github.fanzh.common.core.properties.SysProperties;
import com.github.fanzh.common.core.utils.JsonUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.TreeUtil;
import com.github.fanzh.common.security.constant.SecurityConstant;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.user.api.constant.MenuConstant;
import com.github.fanzh.user.api.dto.MenuDto;
import com.github.fanzh.user.api.module.Menu;
import com.github.fanzh.user.api.module.Role;
import com.github.fanzh.user.api.module.RoleMenu;
import com.github.fanzh.user.mapper.MenuMapper;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单service
 *
 * @author fanzh
 * @date 2018/8/26 22:45
 */
@AllArgsConstructor
@Service
public class MenuService extends BaseService<MenuMapper, Menu> {

    private final RoleMenuService roleMenuService;
    private final SysProperties sysProperties;
    private final RoleService roleService;

    /**
     * 返回当前用户的树形菜单集合
     *
     * @return List
     * @author fanzh
     * @date 2019-09-14 14:41
     */
    public List<MenuDto> findUserMenu() {
        List<MenuDto> menuDtoList = new ArrayList<>();
        String tenantCode = SysUtil.getTenantCode();
        String identifier = SysUtil.getUser();
        List<Menu> userMenus;
        // 查询默认租户的菜单
        Menu condition = new Menu();
        condition.setDelFlag(CommonConstant.DEL_FLAG_NORMAL);
        condition.setTenantCode(SecurityConstant.DEFAULT_TENANT_CODE);
        condition.setApplicationCode(SysUtil.getSysCode());
        condition.setType(MenuConstant.MENU_TYPE_MENU);
        List<Menu> defaultMenus = baseList(condition);

        // 超级管理员
        if (identifier.equals(sysProperties.getAdminUser())) {
            // 获取租户的菜单和默认租户的菜单，最后组装数据，租户的菜单优先
            if (SecurityConstant.DEFAULT_TENANT_CODE.equals(tenantCode)) {
                userMenus = defaultMenus;
            } else {
                // 获取角色的菜单
                condition.setTenantCode(tenantCode);
                condition.setApplicationCode(SysUtil.getSysCode());
                condition.setType(MenuConstant.MENU_TYPE_MENU);
                List<Menu> tenantMenus = baseList(condition);
                // 组装数据
                userMenus = mergeMenu(defaultMenus, tenantMenus);
            }
        } else {
            List<Role> roleList = SysUtil.getCurrentAuthentication().getAuthorities().stream()
                    // 按角色过滤
                    .filter(authority -> authority.getAuthority() != null && authority.getAuthority()
                            .startsWith("ROLE_")).map(authority -> {
                        Role role = new Role();
                        role.setRoleCode(authority.getAuthority());
                        return role;
                    }).collect(Collectors.toList());
            // 根据角色code批量查找菜单
            List<Menu> tenantMenus = finMenuByRoleList(roleList, tenantCode);
            // 组装数据
            userMenus = mergeMenu(getTenantMenus(defaultMenus), tenantMenus);
        }
        if (ParamsUtil.isEmpty(userMenus)) {
            return Collections.emptyList();
        }
        userMenus.stream()
                // 菜单类型
                .filter(menu -> MenuConstant.MENU_TYPE_MENU.equals(menu.getType()))
                // dto封装
                .map(o -> JsonUtil.objToObj(o, MenuDto.class))
                // 去重-
                .distinct().forEach(menuDtoList::add);
        // 排序、构建树形关系
        return TreeUtil.buildTree(CollUtil.sort(menuDtoList, Comparator.comparingInt(MenuDto::getSort)), CommonConstant.ROOT);
    }

    /**
     * 根据角色查找菜单
     *
     * @param roleCode   角色标识
     * @param tenantCode 租户标识
     * @return List
     * @author fanzh
     * @date 2018/8/27 16:00
     */
    public List<Menu> findMenuByRole(String roleCode, String tenantCode) {
        List<Menu> menus = new ArrayList<>();
        // 返回默认租户的角色菜单
        Set<String> tenantCodeList = new HashSet<>();
        tenantCodeList.add(SecurityConstant.DEFAULT_TENANT_CODE);
        tenantCodeList.add(tenantCode);
        EntityWrapper<Role> rEw = new EntityWrapper<>();
        rEw.eq(SqlField.DEL_FLAG, CommonConstant.DEL_FLAG_NORMAL);
        rEw.eq("role_code", roleCode);
        rEw.in(SqlField.TENANT_CODE, tenantCodeList);
        List<Role> roleList = roleService.selectList(rEw);
        if (ParamsUtil.isEmpty(roleList)) {
            return menus;
        }
        EntityWrapper<RoleMenu> rmEw = new EntityWrapper<>();
        rmEw.eq(SqlField.DEL_FLAG, CommonConstant.DEL_FLAG_NORMAL);
        rmEw.in("role_id", roleList.stream().map(o -> o.getId()).collect(Collectors.toList()));
        List<RoleMenu> roleMenuList = roleMenuService.selectList(rmEw);
        if (ParamsUtil.isEmpty(roleMenuList)) {
            return menus;
        }
        EntityWrapper<Menu> mEw = new EntityWrapper<>();
        mEw.eq(SqlField.DEL_FLAG, CommonConstant.DEL_FLAG_NORMAL);
        mEw.in(SqlField.ID, roleMenuList.stream().map(o -> o.getMenuId()).collect(Collectors.toList()));
        mEw.orderDesc(Arrays.asList(SqlField.SORT));
        menus = this.selectList(mEw);
        return menus;
    }

    /**
     * 批量查询菜单
     *
     * @param roleList   roleList
     * @param tenantCode tenantCode
     * @return List
     * @author fanzh
     * @date 2019/07/03 23:50:35
     */
    private List<Menu> finMenuByRoleList(List<Role> roleList, String tenantCode) {
        List<Menu> menus = Lists.newArrayList();
        for (Role role : roleList) {
            List<Menu> roleMenus = this.findMenuByRole(role.getRoleCode(), tenantCode);
            if (CollectionUtils.isNotEmpty(roleMenus)) {
                menus.addAll(roleMenus);
            }
        }
        return menus;
    }

    /**
     * 查询全部菜单，包括租户本身的菜单和默认租户的菜单
     *
     * @param tenantCode
     * @param applicationCode
     * @return
     */
    public List<Menu> find(
            Optional<String> tenantCode
            , Optional<String> applicationCode
            , Optional<Integer> type
    ) {
        Set<String> tenantCodeList = new HashSet<>();
        tenantCodeList.add(SecurityConstant.DEFAULT_TENANT_CODE);
        tenantCode.ifPresent(tenantCodeList::add);
        EntityWrapper<Menu> ew = EntityWrapperUtil.build();
        applicationCode.ifPresent(s -> ew.eq(SqlField.APPLICATION_CODE, s));
        ew.in(SqlField.TENANT_CODE, tenantCodeList);
        type.ifPresent(s -> ew.eq(SqlField.TYPE, s));
        return selectList(ew);
    }

    /**
     * 合并默认租户和租户的菜单，租户菜单优先
     *
     * @param defaultMenus defaultMenus
     * @param tenantMenus  tenantMenus
     * @return List
     * @author fanzh
     * @date 2019-09-14 14:45
     */
    private List<Menu> mergeMenu(List<Menu> defaultMenus, List<Menu> tenantMenus) {
        if (ParamsUtil.isEmpty(tenantMenus)) {
            return defaultMenus;
        }
        List<Menu> userMenus = new ArrayList<>();
        // 默认菜单
        defaultMenus.forEach(defaultMenu -> {
            Optional<Menu> menu = tenantMenus.stream()
                    .filter(tenantMenu -> tenantMenu.getName().equals(defaultMenu.getName())).findFirst();
            if (menu.isPresent()) {
                userMenus.add(menu.get());
            } else {
                userMenus.add(defaultMenu);
            }
        });
        // 租户菜单
        tenantMenus.forEach(tenantMenu -> {
            Optional<Menu> exist = userMenus.stream()
                    .filter(userMenu -> userMenu.getName().equals(tenantMenu.getName()) && userMenu.getParentId()
                            .equals(tenantMenu.getParentId())).findFirst();
            if (!exist.isPresent()) {
                userMenus.add(tenantMenu);
            }
        });
        return userMenus;
    }


    /**
     * 获取租户权限的菜单
     *
     * @param defaultMenus defaultMenus
     * @return List
     */
    private List<Menu> getTenantMenus(List<Menu> defaultMenus) {
        List<Menu> tenantMenus = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(defaultMenus)) {
            defaultMenus.forEach(menu -> {
                String permission = menu.getPermission();
                // 过滤客户端管理、路由管理、系统监控菜单
                if (!permission.equals(MenuConstant.MENU_CLIENT) && !permission.equals(MenuConstant.MENU_ROUTE)
                        && !permission.equals(MenuConstant.MENU_TENANT) && !permission
                        .equals(MenuConstant.MENU_MONITOR)) {
                    tenantMenus.add(menu);
                }
            });
        }
        return tenantMenus;
    }

    /**
     * 返回树形菜单集合
     *
     * @return 树形菜单集合
     */
    public List<MenuDto> treeMenus() {
        // 查询所有菜单
        Menu condition = new Menu();
        condition.setApplicationCode(SysUtil.getSysCode());
        condition.setTenantCode(SysUtil.getTenantCode());
        List<Menu> list = this.baseList(condition);
        // 转成MenuDto
        List<MenuDto> menuDtoList = JsonUtil.listToList(list, MenuDto.class);
        // 排序、构建树形关系
        return TreeUtil.buildTree(CollUtil.sort(menuDtoList, Comparator.comparingInt(MenuDto::getSort)), CommonConstant.ROOT);
    }


}
