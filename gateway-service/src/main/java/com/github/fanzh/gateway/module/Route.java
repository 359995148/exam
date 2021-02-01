package com.github.fanzh.gateway.module;

import com.baomidou.mybatisplus.annotations.TableName;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * 路由信息
 *
 * @author fanzh
 * @date 2019/4/2 14:52
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_route")
public class Route extends BaseEntity {

    /**
     * 路由ID
     */
    @NotBlank(message = "路由id不能为空")
    private String routeId;

    /**
     * 路由名称
     */
    private String routeName;

    /**
     * 断言
     */
    private String predicates;

    /**
     * 过滤器
     */
    private String filters;

    /**
     * URI
     */
    @NotBlank(message = "路由URI不能为空")
    private String uri;

    /**
     * 排序
     */
    private String sort;

    /**
     * 启用禁用
     */
    @NotBlank(message = "路由状态不能为空")
    private String status;
}
