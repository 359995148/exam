package com.github.fanzh.user.api.module;

import com.baomidou.mybatisplus.annotations.TableName;
import com.github.fanzh.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 学生
 *
 * @author fanzh
 * @date 2019/07/09 15:08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_student")
public class Student extends BaseEntity {

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 出生日期
     */
    private Date born;

    /**
     * 电话号码
     */
    private String phone;

    /**
     * 微信
     */
    private String wechat;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 家庭住址
     */
    private String address;

    /**
     * 就读年级
     */
    private String grade;

    /**
     * 就读班级名称
      */
    private String gradeName;

    /**
     * 就读学校
     */
    private String school;

    /**
     * 城市id
     */
    private String cityId;

    /**
     * 县id
     */
    private String countyId;
}
