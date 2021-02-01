package com.github.fanzh.common.core.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Optional 工具类
 *
 * @author fanzh
 */
public class OptionalUtil {

    private OptionalUtil() {
    }


    public static Optional build(String i) {
        if (StringUtils.isEmpty(i)) {
            return Optional.empty();
        }
        return Optional.of(i);
    }

    public static Optional build(Integer i) {
        return Optional.ofNullable(i);
    }

    public static Optional build(Long i) {
        if (Objects.isNull(i)) {
            return Optional.empty();
        }
        return Optional.of(i);
    }

    public static Optional build(BigDecimal i) {
        if (Objects.isNull(i)) {
            return Optional.empty();
        }
        return Optional.of(i);
    }

    public static Optional build(Object i) {
        if (Objects.isNull(i)) {
            return Optional.empty();
        }
        return Optional.of(i);
    }

    public static Optional build(Collection<?> i) {
        if (CollectionUtils.isEmpty(i)) {
            return Optional.empty();
        }
        return Optional.of(i);
    }
}