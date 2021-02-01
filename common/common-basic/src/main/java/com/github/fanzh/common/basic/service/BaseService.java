package com.github.fanzh.common.basic.service;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.CollectionUtils;
import com.github.fanzh.common.basic.annotations.SqlLikeLabel;
import com.github.fanzh.common.basic.id.IdGen;
import com.github.fanzh.common.basic.utils.EntityWrapperUtil;
import com.github.fanzh.common.core.entity.BaseEntity;
import com.github.fanzh.common.core.entity.PageEntity;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.constant.SqlField;
import com.github.fanzh.common.core.exceptions.CommonException;
import com.github.fanzh.common.security.utils.SysUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author fanzh
 * @date 2018-08-25 17:22
 */
@Slf4j
@Component
@NoArgsConstructor
public abstract class BaseService<M extends BaseMapper<T>, T extends BaseEntity> extends ServiceImpl<M, T> {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    /**
     * 保存
     *
     * @param info
     */
    @Transactional(rollbackFor = Throwable.class)
    public void baseSave(T info) {
        if (ParamsUtil.isEmpty(info)) {
            return;
        }
        baseSave(Arrays.asList(info));
    }

    /**
     * 保存
     *
     * @param list
     */
    @Transactional(rollbackFor = Throwable.class)
    public void baseSave(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        String user = SysUtil.getUser();
        String applicationCode = SysUtil.getSysCode();
        String tenantCode = SysUtil.getTenantCode();
        List<Long> oldIds = new ArrayList<>();
        list.forEach(o -> {
            if (ParamsUtil.isEmpty(o.getId())) {
                o.setId(IdGen.snowflakeId());
            } else {
                oldIds.add(o.getId());
            }
            if (ParamsUtil.isEmpty(o.getModifier())) {
                o.setModifier(user);
            }
            o.setCreateDate(null);
            o.setModifyDate(null);
        });
        List<T> oldInfos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(oldIds)) {
            oldInfos = selectBatchIds(oldIds);
        }
        Map<Long, T> mapById = oldInfos.stream().collect(Collectors.toMap(o -> o.getId(), o -> o));
        List<T> addInfos = new ArrayList<>();
        List<T> updateInfos = new ArrayList<>();
        for (T info : list) {
            if (mapById.containsKey(info.getId())) {
                updateInfos.add(info);
            } else {
                if (ParamsUtil.isEmpty(info.getCreator())) {
                    info.setCreator(user);
                }
                if (ParamsUtil.isEmpty(info.getApplicationCode())) {
                    info.setApplicationCode(applicationCode);
                }
                if (ParamsUtil.isEmpty(info.getTenantCode())) {
                    info.setTenantCode(tenantCode);
                }
                addInfos.add(info);
            }
        }
        if (CollectionUtils.isNotEmpty(addInfos)) {
            insertBatch(addInfos);
        }
        if (CollectionUtils.isNotEmpty(updateInfos)) {
            updateBatchById(updateInfos);
        }
        //清空一级缓存
        sqlSessionTemplate.clearCache();
    }

    /**
     * 更新
     *
     * @param t
     */
    @Transactional(rollbackFor = Throwable.class)
    public void baseUpdate(T t) {
        if (ParamsUtil.isEmpty(t)) {
            return;
        }
        baseGetById(t.getId());
        baseSave(t);
    }

    /**
     * 物理删除
     *
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public void baseDelete(Long id) {
        if (ParamsUtil.isEmpty(id)) {
            return;
        }
        baseDelete(SetUtil.build(id));
    }

    /**
     * 物理删除
     *
     * @param ids
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public void baseDelete(Set<Long> ids) {
        if (ParamsUtil.isEmpty(ids)) {
            return;
        }
        List<T> list = baseFindById(ids);
        if (ParamsUtil.isEmpty(list)) {
            return;
        }
        this.deleteBatchIds(list.stream().map(o -> o.getId()).collect(Collectors.toSet()));
    }

    public Optional<T> baseFindById(Long id) {
        if (ParamsUtil.isEmpty(id)) {
            return Optional.empty();
        }
        return baseFindById(SetUtil.build(id)).stream().findAny();
    }

    public List<T> baseFindById(Set<Long> id) {
        if (ParamsUtil.isEmpty(id)) {
            return Collections.emptyList();
        }
        EntityWrapper<T> ew = EntityWrapperUtil.build();
        ew.in(SqlField.ID, id);
        return selectList(ew);
    }

    /**
     * 逻辑删除
     *
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public Optional<Long> baseLogicDelete(Long id) {
        if (ParamsUtil.isEmpty(id)) {
            return Optional.empty();
        }
        return baseLogicDelete(SetUtil.build(id)).stream().findAny();
    }

    /**
     * 逻辑删除
     *
     * @param ids
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public List<Long> baseLogicDelete(Set<Long> ids) {
        if (ParamsUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        EntityWrapper<T> ew = EntityWrapperUtil.build();
        ew.in(SqlField.ID, ids);
        List<T> list = selectList(ew);
        list.forEach(o -> o.setDelFlag(CommonConstant.DEL_FLAG_DEL));
        baseSave(list);
        return list.stream().map(o -> o.getId()).collect(Collectors.toList());
    }

    /**
     * 获取
     *
     * @param id
     * @return
     */
    public T baseGetById(Long id) {
        return baseGetById(id, Optional.of(true));
    }

    /**
     * 获取
     *
     * @param id
     * @return
     */
    public T baseGetById(Long id, Optional<Boolean> checkDel) {
        if (ParamsUtil.isEmpty(id)) {
            throw new CommonException("Method baseGetById() param cannot be empty");
        }
        T t = selectById(id);
        if (ParamsUtil.isEmpty(t)) {
            throw new CommonException("Method baseGetById() param is invalid by ID: " + id);
        }
        if (checkDel.orElse(true)) {
            //默认校验删除标记
            if (!Objects.equals(CommonConstant.DEL_FLAG_NORMAL, t.getDelFlag())) {
                //不为正常状态, 视为删除状态
                throw new CommonException("Method baseGetById() data is deleted by ID: " + id);
            }
        }
        return t;
    }

    /**
     * 构建实体查询包装
     *
     * @param t
     * @return
     */
    private EntityWrapper<T> buildEw(T t) {
        EntityWrapper<T> ew = new EntityWrapper<>();
        ew.eq(SqlField.DEL_FLAG, CommonConstant.DEL_FLAG_NORMAL);
        if (ParamsUtil.isEmpty(t)) {
            return ew;
        }
        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object o = field.get(t);
                if (ParamsUtil.isEmpty(o)) {
                    continue;
                }
                //字段是否为模糊查询
                SqlLikeLabel ppl = field.getAnnotation(SqlLikeLabel.class);
                if (ParamsUtil.isEmpty(ppl)) {
                    if (o instanceof Collection<?>) {
                        ew.in(ParamsUtil.camelToUnderline(field.getName()), Collections.singletonList(o));
                    } else {
                        ew.eq(ParamsUtil.camelToUnderline(field.getName()), o);
                    }
                } else {
                    if (o instanceof java.lang.String) {
                        ew.like(ParamsUtil.camelToUnderline(field.getName()), o.toString());
                    } else {
                        throw new CommonException(String.format("Method buildEw() Field: %s, Parameter type error: %s", field.getName(), "like field can only be string"));
                    }
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
        return ew;
    }

    /**
     * 列表查询
     *
     * @param t 参数
     * @return
     */
    public List<T> baseList(T t) {
        return baseListOrPage(Optional.empty(), buildEw(t)).getRecords();
    }

    /**
     * 分页查询
     *
     * @param p 分页
     * @param t 参数
     * @return
     */
    public Page<T> baseListOrPage(Optional<PageEntity> p, T t) {
        return baseListOrPage(p, buildEw(t));
    }

    /**
     * 分页查询
     *
     * @param p  分页
     * @param ew 参数
     * @return
     */
    public Page<T> baseListOrPage(Optional<PageEntity> p, EntityWrapper<T> ew) {
        Page<T> page = new Page<>();
        if (p.isPresent()) {
            if (ParamsUtil.isNotEmpty(p.get().getSort())) {
                if (ParamsUtil.isEmpty(p.get().getOrder()) || Objects.equals(p.get().getOrder(), CommonConstant.PAGE_ORDER_DEFAULT)) {
                    ew.orderDesc(Arrays.asList(p.get().getSort()));
                } else {
                    ew.orderAsc(Arrays.asList(p.get().getSort()));
                }
            }
            if (ParamsUtil.isNotEmpty(p.get().getPageNum()) && ParamsUtil.isNotEmpty(p.get().getPageSize())) {
                page = selectPage(new Page(p.get().getPageNum(), p.get().getPageSize()), ew);
            } else {
                page.setRecords(selectList(ew));
            }
        } else {
            page.setRecords(selectList(ew));
        }
        return page;
    }

}

