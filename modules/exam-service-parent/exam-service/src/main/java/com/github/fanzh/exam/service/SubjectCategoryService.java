package com.github.fanzh.exam.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.fanzh.exam.mapper.SubjectCategoryMapper;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.constant.SqlField;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.basic.utils.EntityWrapperUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.TreeUtil;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.exam.api.dto.SubjectCategoryDto;
import com.github.fanzh.exam.api.module.SubjectCategory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目分类service
 *
 * @author fanzh
 * @date 2018/12/4 21:56
 */
@Service
public class SubjectCategoryService extends BaseService<SubjectCategoryMapper, SubjectCategory> {


    /**
     * 返回树形分类集合
     *
     * @return List
     * @author fanzh
     * @date 2018/12/04 22:03
     */
    public List<SubjectCategoryDto> menus() {
        SubjectCategory subjectCategory = new SubjectCategory();
        subjectCategory.setTenantCode(SysUtil.getTenantCode());
        EntityWrapper<SubjectCategory> ew = EntityWrapperUtil.build();
        ew.eq(SqlField.TENANT_CODE, SysUtil.getTenantCode());
        ew.orderDesc(Arrays.asList(SqlField.SORT));
        // 查询所有分类
        List<SubjectCategory> subjectCategoryList = this.selectList(ew);
        if (ParamsUtil.isEmpty(subjectCategoryList)) {
            return Collections.emptyList();
        }
        // 转成dto
        List<SubjectCategoryDto> subjectCategorySetTreeList = subjectCategoryList.stream()
                .map(SubjectCategoryDto::new).distinct().collect(Collectors.toList());
        // 排序、组装树形结构
        return TreeUtil.buildTree(CollUtil.sort(subjectCategorySetTreeList, Comparator.comparingInt(SubjectCategoryDto::getSort)), CommonConstant.ROOT);
    }
}
