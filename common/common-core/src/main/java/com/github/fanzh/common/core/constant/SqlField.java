package com.github.fanzh.common.core.constant;

/**
 * @author fanzh
 * @describe 数据库字段公用key
 */
public class SqlField {

    private SqlField() {
    }

    /**
     * 主键id
     */
    public static final String ID = "id";
    /**
     * 删除
     */
    public static final String DEL_FLAG = "del_flag";
    /**
     * 创建人
     */
    public static final String CREATOR = "creator";
    /**
     * 更新人
     */
    public static final String MODIFIER = "modifier";
    /**
     * 创建日期
     */
    public static final String CREATE_DATE = "create_date";
    /**
     * 更新时间
     */
    public static final String MODIFY_DATE = "modify_date";
    /**
     * 排序字段
     */
    public static final String SORT = "sort";
    /**
     * 系统编号
     */
    public static final String APPLICATION_CODE = "application_code";
    /**
     * 系统编号
     */
    public static final String TENANT_CODE = "tenant_code";
    /**
     * 状态
     */
    public static final String STATUS = "status";
    /**
     * 类型
     */
    public static final String TYPE = "type";
    /**
     * 用户ID
     */
    public static final String USER_ID = "user_id";

}
