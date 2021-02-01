package com.github.fanzh.common.core.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分页实体
 *
 * @author fanzh
 * @date 2018-09-13 20:40
 */
@Data
@NoArgsConstructor
public class PageEntity implements Serializable {

    private static final long serialVersionUID = 7265456426423066026L;

    @ApiModelProperty(value = "页数")
    private Integer pageNum;

    @ApiModelProperty(value = "页长")
    private Integer pageSize;

    @ApiModelProperty(value = "排序字段")
    private String sort;

    @ApiModelProperty(value = "排序方向 desc倒, asc正")
    private String order;
}

