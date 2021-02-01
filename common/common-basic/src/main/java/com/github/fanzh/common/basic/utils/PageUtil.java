package com.github.fanzh.common.basic.utils;

import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.utils.OptionalUtil;
import org.springframework.beans.BeanUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 分页查询工具类
 *
 * @author fanzh
 * @date 2018/12/4 20:16
 */
public class PageUtil {


    public static <T> Page<T> pageInfo(String pageNum, String pageSize) {
        return pageInfo(pageNum, pageSize, Optional.empty(), Optional.empty());
    }

    public static <T> Page<T> pageInfo(String pageNum, String pageSize, String sort, String order) {
        return pageInfo(pageNum, pageSize, OptionalUtil.build(sort), OptionalUtil.build(order));
    }

    public static <T> Page<T> pageInfo(String pageNum, String pageSize, Optional<String> sort, Optional<String> order) {
        Page<T> page = new Page<>();
        page.setCurrent(Integer.parseInt(pageNum));
        page.setSize(Integer.parseInt(pageSize));
        if (sort.isPresent() && order.isPresent()) {
            if (Objects.equals(CommonConstant.PAGE_ORDER_DEFAULT, order.get())) {
                page.setDescs(Arrays.asList(sort.get()));
            } else {
                page.setAscs(Arrays.asList(sort.get()));
            }
        }
        return page;
    }

    public static <T> Page<T> copy(Page source) {
        source.setRecords(null);
        Page target = new Page();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
