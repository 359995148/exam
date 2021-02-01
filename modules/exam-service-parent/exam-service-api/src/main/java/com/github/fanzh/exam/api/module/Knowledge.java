package com.github.fanzh.exam.api.module;

import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 知识库
 *
 * @author fanzh
 * @date 2019/1/1 15:01
 */
@Data
@TableName(value = "exam_knowledge")
public class Knowledge extends BaseEntity {

    /**
     * 知识名称
     */
    @NotBlank(message = "知识名称不能为空")
    private String knowledgeName;

    /**
     * 知识描述
     */
    private String knowledgeDesc;

    /**
     * 附件ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long attachmentId;

    /**
     * 状态
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}
