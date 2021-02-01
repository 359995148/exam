package com.github.fanzh.user.api.module;

import com.baomidou.mybatisplus.annotations.TableName;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户学生关联关系，一个用户可以绑定多个学生
 *
 * @author fanzh
 * @date 2019/07/09 15:23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_student")
public class UserStudent extends BaseEntity {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 学生id
     */
    private Long studentId;

    /**
     * 关系类型
     */
    private Integer relationshipType;
}
