package com.github.fanzh.user.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.fanzh.common.core.entity.TreeEntity;
import lombok.Data;

/**
 * 菜单dto
 *
 * @author fanzh
 * @date 2018-09-13 20:39
 */
@Data
public class MenuDto extends TreeEntity<MenuDto> {

    /**
     * 父菜单ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long parentId;

    private String icon;

    private String name;

    private String url;

    private String redirect;

    private boolean spread = false;

    private String path;

    private String component;

    private String authority;

    private String code;

    private Integer type;

    private String[] roles;

    private String remark;
}
