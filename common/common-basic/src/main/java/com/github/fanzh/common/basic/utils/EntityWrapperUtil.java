package com.github.fanzh.common.basic.utils;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.constant.SqlField;

/**
 * EntityWrapper 工具类
 *
 * @author fanzh
 */
public class EntityWrapperUtil {

    private EntityWrapperUtil() {
    }


    public static <T> EntityWrapper<T> build() {
        EntityWrapper<T> ew = new EntityWrapper();
        ew.eq(SqlField.DEL_FLAG, CommonConstant.DEL_FLAG_NORMAL);
        return ew;
    }


}