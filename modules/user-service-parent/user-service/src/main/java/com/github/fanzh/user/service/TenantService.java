package com.github.fanzh.user.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.constant.SqlField;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.basic.utils.EntityWrapperUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.security.constant.SecurityConstant;
import com.github.fanzh.user.api.constant.TenantConstant;
import com.github.fanzh.user.api.enums.IdentityType;
import com.github.fanzh.user.api.module.User;
import com.github.fanzh.user.api.module.Menu;
import com.github.fanzh.user.api.module.Role;
import com.github.fanzh.user.api.module.Tenant;
import com.github.fanzh.user.api.module.UserAuths;
import com.github.fanzh.user.api.module.UserRole;
import com.github.fanzh.user.mapper.TenantMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 租户Service
 *
 * @author fanzh
 * @date 2019/5/22 22:51
 */
@Slf4j
@AllArgsConstructor
@Service
public class TenantService extends BaseService<TenantMapper, Tenant> {

    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private final UserService userService;

    private final UserAuthsService userAuthsService;

    private final UserRoleService userRoleService;

    private final RoleService roleService;

    private final MenuService menuService;

    /**
     * 根据租户标识获取
     *
     * @param tenantCode tenantCode
     * @return Tenant
     * @author fanzh
     * @date 2019/05/26 10:28
     */
    public Tenant findByTenantCode(String tenantCode) {
        if (ParamsUtil.isEmpty(tenantCode)) {
            return null;
        }
        EntityWrapper<Tenant> ew = new EntityWrapper<>();
        ew.eq(SqlField.DEL_FLAG, CommonConstant.DEL_FLAG_NORMAL);
        ew.eq(SqlField.TENANT_CODE, tenantCode);
        return selectOne(ew);
    }

    /**
     * 新增租户，自动初始化租户管理员账号
     *
     * @param tenant tenant
     * @return int
     * @author fanzh
     * @date 2019-09-02 11:41
     */
    @Transactional(rollbackFor = Throwable.class)
    @CacheEvict(value = "tenant", key = "#tenant.tenantCode")
    public void addTenant(Tenant tenant) {
        this.baseSave(tenant);
    }

    /**
     * 更新
     *
     * @param tenant tenant
     * @return Tenant
     * @author fanzh
     * @date 2019/05/26 10:28
     */
    @CacheEvict(value = "tenant", key = "#tenant.tenantCode")
    @Transactional
    public int updateTenant(Tenant tenant) {
        Integer status = tenant.getStatus();
        Tenant currentTenant = this.baseGetById(tenant.getId());
        // 待审核 -> 审核通过
        if (currentTenant != null && currentTenant.getStatus().equals(TenantConstant.PENDING_AUDIT) && status.equals(TenantConstant.APPROVAL)) {
            log.info("Pending review -> review passed：{}", tenant.getTenantCode());
            // 用户基本信息
            User user = new User();
            user.setTenantCode(tenant.getTenantCode());
            user.setStatus(CommonConstant.STATUS_NORMAL);
            user.setName(tenant.getTenantName());
            userService.baseSave(user);
            // 用户账号
            UserAuths userAuths = new UserAuths();
            userAuths.setUserId(user.getId());
            userAuths.setIdentifier(tenant.getTenantCode());
            userAuths.setIdentityType(IdentityType.PASSWORD.getValue());
            userAuths.setCredential(encoder.encode(CommonConstant.DEFAULT_PASSWORD));
            userAuthsService.baseSave(userAuths);
            // 绑定角色
            Role role = roleService.getByRoleCode(SecurityConstant.ROLE_TENANT_ADMIN);
            UserRole userRole = new UserRole();
            userRole.setTenantCode(tenant.getTenantCode());
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            userRoleService.baseSave(userRole);
        }
        this.baseUpdate(tenant);
        return 1;
    }

    /**
     * 删除
     *
     * @param id
     */
    @CacheEvict(value = "tenant", key = "#tenant.tenantCode")
    @Transactional(rollbackFor = Throwable.class)
    public void deleteTenant(Set<Long> id) {
        List<Tenant> tenant = this.selectBatchIds(id);
        tenant = tenant.stream().filter(o -> Objects.equals(o.getDelFlag(), CommonConstant.DEL_FLAG_NORMAL)).collect(Collectors.toList());
        if (ParamsUtil.isEmpty(tenant)) {
            return;
        }
        // 删除菜单
        EntityWrapper ew = EntityWrapperUtil.build();
        ew.in(SqlField.TENANT_CODE, tenant.stream().map(o -> o.getTenantCode()).collect(Collectors.toSet()));
        List<Menu> menuList = menuService.selectList(ew);
        menuService.baseLogicDelete(menuList.stream().map(o -> o.getId()).collect(Collectors.toSet()));
        //删除用户
        List<User> userList = userService.selectList(ew);
        userService.baseLogicDelete(userList.stream().map(o -> o.getId()).collect(Collectors.toSet()));
        // TODO 删除权限

        this.baseLogicDelete(id);
    }

    /**
     * 查询单位数量
     *
     * @return Integer
     * @author fanzh
     * @date 2019/12/18 5:09 下午
     */
    public Integer tenantCount() {
        EntityWrapper<Tenant> ew = EntityWrapperUtil.build();
        return this.selectCount(ew);
    }
}
