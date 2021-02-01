package com.github.fanzh.gateway.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.entity.PageEntity;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.OptionalUtil;
import com.github.fanzh.gateway.module.Route;
import com.github.fanzh.gateway.service.RouteService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 路由controller
 * TODO：增加security认证
 *
 * @author fanzh
 * @date 2019/4/2 15:03
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/route/v1/route")
public class RouteController {

    private final RouteService routeService;


    @GetMapping("/{id}")
    public ExecResult<Route> get(@PathVariable Long id) {
        return ExecResultUtil.success(routeService.baseGetById(id));
    }

    @GetMapping("/list-page")
    public ExecResult<Page<Route>> listOrPage(PageEntity pageEntity, Route route) {
        return ExecResultUtil.success(routeService.baseListOrPage(OptionalUtil.build(pageEntity), route));
    }

    @PostMapping
    public ExecResult<Boolean> add(@RequestBody @Valid Route route) {
        return routeService.add(route);
    }

    @PutMapping
    public ExecResult<Boolean> update(@RequestBody @Valid Route route) {
        return routeService.update(route);
    }

    @DeleteMapping("/{id}")
    public ExecResult<Boolean> delete(@PathVariable Long id) {
        return routeService.delete(id);
    }

    @DeleteMapping("/list")
    public ExecResult<Boolean> listDelete(@RequestBody List<Long> ids) {
        return routeService.delete(ids);
    }

    @GetMapping("/refresh")
    public ExecResult<Boolean> refresh() {
        return routeService.refresh();
    }
}
