package com.github.fanzh.common.basic.vo;

import com.github.fanzh.common.core.model.Log;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;

/**
 * logVo
 *
 * @author fanzh
 * @date 2019-01-05 17:07
 */
@Data
public class LogVo extends BaseEntity {

    private Log log;

    private String username;
}
