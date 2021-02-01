package com.github.fanzh.common.core.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author fanzh
 * @date 2019/07/05 15:05
 */
@Data
public class SysConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 上传地址
     */
    private String uploadUrl;

    /**
     * 默认头像
     */
    private String defaultAvatar;

    /**
     * 管理员账号
     */
    private String adminUser;
}
