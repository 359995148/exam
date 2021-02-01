package com.github.fanzh.user.api.module;

import com.baomidou.mybatisplus.annotations.TableName;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户角色关系
 *
 * @author fanzh
 * @date 2018/8/26 09:29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_role")
public class UserRole extends BaseEntity {

    private Long userId;

    private Long roleId;
}
