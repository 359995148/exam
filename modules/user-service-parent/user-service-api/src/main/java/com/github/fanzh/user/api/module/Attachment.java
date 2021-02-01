package com.github.fanzh.user.api.module;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.fanzh.common.core.entity.BaseEntity;
import com.github.fanzh.user.api.constant.AttachmentConstant;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 附件信息
 *
 * @author fanzh
 * @date 2018/10/30 20:47
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_attachment")
public class Attachment extends BaseEntity {

    /**
     * 附件名称
     */
    private String attachName;

    /**
     * 附件大小
     */
    private String attachSize;

    /**
     * 附件类型
     */
    private String attachType;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 文件ID
     */
    @JsonIgnore
    private String fastFileId;

    /**
     * 业务流水号
     */
    private String busiId;

    /**
     * 业务类型
     */
    private String busiType = AttachmentConstant.BUSI_TYPE_NORMAL_ATTACHMENT;

    /**
     * 业务模块
     */
    private String busiModule;

    /**
     * 预览地址
     */
    private String previewUrl;

    /**
     * 上传类型，1：本地目录，2：fastDfs，3：七牛云
     */
    private Integer uploadType;

    /**
     * 上传结果
     */
    @TableField(exist = false)
    private String uploadResult;
}
