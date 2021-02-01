package com.github.fanzh.common.basic.vo;

import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;

/**
 * 角色
 *
 * @author fanzh
 * @date 2018-08-25 13:58
 */
@Data
public class RoleVo extends BaseEntity {

    private String roleName;

    private String roleCode;

    private String roleDesc;

}
