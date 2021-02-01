package com.github.fanzh.user.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.constant.SqlField;
import com.github.fanzh.common.core.exceptions.CommonException;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.user.mapper.RoleMapper;
import com.github.fanzh.user.api.module.Role;
import com.github.fanzh.user.api.module.RoleMenu;
import com.github.fanzh.user.api.module.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色service
 *
 * @author fanzh
 * @date 2018/8/26 14:16
 */
@AllArgsConstructor
@Service
public class RoleService extends BaseService<RoleMapper, Role> {

    private final RoleMenuService roleMenuService;
    private final UserRoleService userRoleService;

    @Transactional(rollbackFor = Throwable.class)
    public void delete(List<Long> ids) {
        if (ParamsUtil.isEmpty(ids)) {
            return;
        }
        EntityWrapper ew = new EntityWrapper();
        ew.in("role_id", ids);
        List<RoleMenu> roleMenuList = roleMenuService.selectList(ew);
        if (ParamsUtil.isNotEmpty(roleMenuList)) {
            roleMenuService.deleteBatchIds(roleMenuList.stream().map(o -> o.getId()).collect(Collectors.toList()));
        }
        List<UserRole> userRoleList = userRoleService.selectList(ew);
        if (ParamsUtil.isNotEmpty(userRoleList)) {
            userRoleService.deleteBatchIds(userRoleList.stream().map(o -> o.getId()).collect(Collectors.toList()));
        }
        this.deleteBatchIds(ids);
    }

    public Role getByRoleCode(String roleCode) {
        if (ParamsUtil.isEmpty(roleCode)) {
            throw new CommonException("Method roleService.getByRoleCode() param cannot be empty");
        }
        EntityWrapper<Role> ew = new EntityWrapper<>();
        ew.eq(SqlField.DEL_FLAG, CommonConstant.DEL_FLAG_NORMAL);
        ew.eq("role_code", roleCode);
        Role role = selectOne(ew);
        if (ParamsUtil.isEmpty(role)) {
            throw new CommonException("Method roleService.getByRoleCode() param is invalid");
        }
        return role;
    }

    public List<Role> findByUserId(Long userId) {
        if (ParamsUtil.isEmpty(userId)) {
            return Collections.emptyList();
        }
        return findByUserId(SetUtil.build(userId));
    }

    public List<Role> findByUserId(Set<Long> userId) {
        if (ParamsUtil.isEmpty(userId)) {
            return Collections.emptyList();
        }
        List<UserRole> userRoleList = userRoleService.findByUserId(userId);
        if (ParamsUtil.isEmpty(userRoleList)) {
            return Collections.emptyList();
        }
        return this.baseFindById(userRoleList.stream().map(o -> o.getRoleId()).collect(Collectors.toSet()));
    }
}
