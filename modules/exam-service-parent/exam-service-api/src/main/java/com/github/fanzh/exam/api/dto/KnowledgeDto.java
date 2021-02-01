package com.github.fanzh.exam.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author fanzh
 * @date 2019/1/1 22:04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KnowledgeDto extends BaseEntity {

    /**
     * 知识名称
     */
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
    private String status;

    /**
     * 附件名称
     */
    private String attachName;

    /**
     * 附件大小
     */
    private String attachSize;
}
