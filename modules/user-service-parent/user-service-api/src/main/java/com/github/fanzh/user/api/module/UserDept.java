package com.github.fanzh.user.api.module;

import com.baomidou.mybatisplus.annotations.TableName;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户部门关系
 *
 * @author fanzh
 * @date 2018/10/27 10:23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_dept")
public class UserDept extends BaseEntity {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 部门ID
     */
    private String deptId;
}
