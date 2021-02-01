package com.github.fanzh.exam.api.module;

import com.baomidou.mybatisplus.annotations.TableName;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;

/**
 * 图片表
 *
 * @author fanzh
 * @date 2019/6/16 13:52
 */
@Data
@TableName(value = "exam_pictures")
public class Pictures extends BaseEntity {

    /**
     * 图片地址
     */
    private String pictureAddress;

    /**
     * 附件ID
     */
    private String attachmentId;
}
