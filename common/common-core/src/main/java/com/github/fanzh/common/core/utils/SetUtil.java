package com.github.fanzh.common.core.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Set工具类
 *
 * @author fanzh
 * @date 2018/12/4 20:16
 */
public class SetUtil {


    public static <T> Set build(T t) {
        if (ParamsUtil.isEmpty(t)) {
            return new HashSet();
        }
        if (t instanceof Collection) {
            return new HashSet((Collection) t);
        }
        return new HashSet<>(Arrays.asList(t));
    }


}
