package com.github.fanzh.gateway.module;

import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 网关过滤器
 *
 * @author fanzh
 * @date 2019-08-16 17:48
 */
@Data
public class Filters extends BaseEntity {

    /**
     * 路由ID
     */
    @NotBlank(message = "路由ID不能为空")
    private String routeId;

    /**
     * 过滤器名称
     */
    @NotBlank(message = "filter name不能为空")
    private String name;

    /**
     * 路由参数
     */
    private String args;
}
