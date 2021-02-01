package com.github.fanzh.common.core.utils;

import com.github.fanzh.common.core.constant.CharacterConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Objects;

/**
 * 参数 工具类
 *
 * @author fanzh
 */
public class ParamsUtil {

    private ParamsUtil() {
    }

    /**
     * 驼峰命名转换成下划线方式名称，eg：testParam > test_param
     *
     * @param param
     * @return
     */
    public static String camelToUnderline(String param) {
        if (ParamsUtil.isEmpty(param)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int len = param.length();
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(CharacterConstant.UNDERLINE);
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    public static boolean isEmpty(String i) {
        return StringUtils.isEmpty(i);
    }

    public static boolean isNotEmpty(String i) {
        return !isEmpty(i);
    }

    public static boolean isEmpty(Long i) {
        return Objects.isNull(i);
    }

    public static boolean isNotEmpty(Long i) {
        return !isEmpty(i);
    }

    public static boolean isEmpty(Integer i) {
        return Objects.isNull(i);
    }

    public static boolean isNotEmpty(Integer i) {
        return !isEmpty(i);
    }

    public static boolean isEmpty(Object i) {
        return Objects.isNull(i);
    }

    public static boolean isNotEmpty(Object i) {
        return !isEmpty(i);
    }

    public static boolean isEmpty(Collection<?> i) {
        return CollectionUtils.isEmpty(i);
    }

    public static boolean isNotEmpty(Collection<?> i) {
        return !isEmpty(i);
    }

}