package com.github.fanzh.user.api.module;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * 角色
 *
 * @author fanzh
 * @date 2018-08-25 13:58
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_role")
public class Role extends BaseEntity {

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色标识不能为空")
    private String roleCode;

    private String roleDesc;

    private Integer status;

    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private String menuIds;

    /**
     * 是否默认 0-否，1-是
     */
    private Integer isDefault;
}
