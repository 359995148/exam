package com.github.fanzh.user.api.module;

import com.baomidou.mybatisplus.annotations.TableName;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色菜单关联
 *
 * @author fanzh
 * @date 2018/8/26 22:24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_role_menu")
public class RoleMenu extends BaseEntity {

    private Long roleId;

    private Long menuId;
}
