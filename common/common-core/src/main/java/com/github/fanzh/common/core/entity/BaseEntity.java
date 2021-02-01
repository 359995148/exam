package com.github.fanzh.common.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.utils.DateUtils;
import com.github.fanzh.common.core.utils.ParamsUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Entity基类
 *
 * @author fanzh
 * @date 2018-08-24 18:58
 */
@Data
@NoArgsConstructor
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    protected Long id;

    /**
     * 创建者
     */
    protected String creator;

    /**
     * 创建日期
     */
    protected Date createDate;

    /**
     * 更新者
     */
    protected String modifier;

    /**
     * 更新日期
     */
    protected Date modifyDate;

    /**
     * 删除标记 0:正常，1-删除
     */
    protected Integer delFlag = CommonConstant.DEL_FLAG_NORMAL;

    /**
     * 系统编号
     */
    protected String applicationCode;

    /**
     * 租户编号
     */
    protected String tenantCode;

    public BaseEntity(Long id) {
        this();
        this.id = id;
    }

    /**
     * 设置基本属性
     *
     * @param userCode        用户编码
     * @param applicationCode 系统编号
     * @param tenantCode      租户编号
     */
    public void setCommonValue(String userCode, String applicationCode, String tenantCode) {
        Date currentDate = DateUtils.asDate(LocalDateTime.now());
        if (ParamsUtil.isEmpty(this.getId())) {
            this.creator = userCode;
            this.createDate = currentDate;
        }
        this.modifier = userCode;
        this.modifyDate = currentDate;
        this.delFlag = CommonConstant.DEL_FLAG_NORMAL;
        this.applicationCode = applicationCode;
        this.tenantCode = tenantCode;
    }

	/**
	 * 置空属性
	 */
	public void clearCommonValue() {
		this.creator = null;
		this.createDate = null;
		this.modifier = null;
		this.modifyDate = null;
    	this.delFlag = null;
    	this.applicationCode = null;
    	this.tenantCode = null;
	}
}

