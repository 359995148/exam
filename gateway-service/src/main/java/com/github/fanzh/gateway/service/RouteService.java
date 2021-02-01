package com.github.fanzh.gateway.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.fasterxml.jackson.databind.JavaType;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.constant.ApiMsg;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.constant.SqlField;
import com.github.fanzh.common.basic.utils.EntityWrapperUtil;
import com.github.fanzh.common.core.utils.JsonMapper;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.gateway.vo.RouteFilterVo;
import com.github.fanzh.gateway.vo.RoutePredicateVo;
import com.github.fanzh.gateway.mapper.RouteMapper;
import com.github.fanzh.gateway.module.Route;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 路由service
 *
 * @author fanzh
 * @date 2019/4/2 15:01
 */
@Slf4j
@AllArgsConstructor
@Service
public class RouteService extends BaseService<RouteMapper, Route> {

    private final DynamicRouteService dynamicRouteService;
    private final RedisTemplate redisTemplate;

    /**
     * 初始化RouteDefinition
     *
     * @param route route
     * @return RouteDefinition
     * @author fanzh
     * @date 2019/04/02 18:50
     */
    private RouteDefinition routeDefinition(Route route) {
        RouteDefinition routeDefinition = new RouteDefinition();
        // id
        routeDefinition.setId(route.getRouteId());
        // predicates
        if (StringUtils.isNotBlank(route.getPredicates())) {
            routeDefinition.setPredicates(predicateDefinitions(route));
        }
        // filters
        if (StringUtils.isNotBlank(route.getFilters())) {
            routeDefinition.setFilters(filterDefinitions(route));
        }
        // uri
        routeDefinition.setUri(URI.create(route.getUri()));
        return routeDefinition;
    }

    /**
     * @param route route
     * @return List
     * @author fanzh
     * @date 2019/04/02 21:28
     */
    private List<PredicateDefinition> predicateDefinitions(Route route) {
        List<PredicateDefinition> predicateDefinitions = new ArrayList<>();
        try {
            List<RoutePredicateVo> routePredicateVoList = JsonMapper.getInstance().fromJson(route.getPredicates(),
                    JsonMapper.getInstance().createCollectionType(ArrayList.class, RoutePredicateVo.class));
            if (CollectionUtils.isNotEmpty(routePredicateVoList)) {
                for (RoutePredicateVo routePredicateVo : routePredicateVoList) {
                    PredicateDefinition predicate = new PredicateDefinition();
                    predicate.setArgs(routePredicateVo.getArgs());
                    predicate.setName(routePredicateVo.getName());
                    predicateDefinitions.add(predicate);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return predicateDefinitions;
    }

    /**
     * @param route route
     * @return List
     * @author fanzh
     * @date 2019/04/02 21:29
     */
    private List<FilterDefinition> filterDefinitions(Route route) {
        List<FilterDefinition> filterDefinitions = new ArrayList<>();
        try {
            JavaType javaType = JsonMapper.getInstance().createCollectionType(ArrayList.class, RouteFilterVo.class);
            List<RouteFilterVo> gatewayFilterDefinitions = JsonMapper.getInstance().fromJson(route.getFilters(), javaType);
            if (ParamsUtil.isEmpty(gatewayFilterDefinitions)) {
                return filterDefinitions;
            }
            for (RouteFilterVo gatewayFilterDefinition : gatewayFilterDefinitions) {
                FilterDefinition filterDefinition = new FilterDefinition();
                filterDefinition.setName(gatewayFilterDefinition.getName());
                filterDefinition.setArgs(gatewayFilterDefinition.getArgs());
                filterDefinitions.add(filterDefinition);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return filterDefinitions;
    }

    /**
     * 新增
     *
     * @param route
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public ExecResult<Boolean> add(Route route) {
        EntityWrapper<Route> ew = new EntityWrapper<>();
        ew.eq(SqlField.DEL_FLAG, CommonConstant.DEL_FLAG_NORMAL);
        ew.eq("route_id", route.getRouteId());
        if (ParamsUtil.isNotEmpty(selectOne(ew))) {
            return ExecResultUtil.error(ApiMsg.KEY_PARAM_VALIDATE, "routeId already exist");
        }
        baseSave(route);
        dynamicRouteService.add(routeDefinition(route));
        return ExecResultUtil.success(true);
    }

    /**
     * 更新
     *
     * @param route
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public ExecResult<Boolean> update(Route route) {
        baseUpdate(route);
        dynamicRouteService.update(routeDefinition(route));
        return ExecResultUtil.success(true);
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public ExecResult<Boolean> delete(Long id) {
        return delete(Arrays.asList(id));
    }

    /**
     * 删除
     *
     * @param ids
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public ExecResult<Boolean> delete(List<Long> ids) {
        List<Long> delIds = this.baseLogicDelete(SetUtil.build(ids));
        for (Long id : delIds) {
            dynamicRouteService.delete(id);
        }
        return ExecResultUtil.success(true);
    }

    /**
     * 刷新
     *
     * @return
     */
    public ExecResult<Boolean> refresh() {
        EntityWrapper<Route> ew = EntityWrapperUtil.build();
        List<Route> routes = this.selectList(ew);
        for (Route route : routes) {
            dynamicRouteService.update(routeDefinition(route));
            // 存入Redis
            redisTemplate.opsForValue().set(CommonConstant.ROUTE_KEY, JsonMapper.getInstance().toJson(routes));
        }
        return ExecResultUtil.success(true);
    }
}
