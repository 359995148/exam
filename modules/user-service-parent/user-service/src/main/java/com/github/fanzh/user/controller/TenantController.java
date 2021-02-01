package com.github.fanzh.user.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.entity.PageEntity;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.OptionalUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.user.api.constant.TenantConstant;
import com.github.fanzh.user.api.module.Tenant;
import com.github.fanzh.user.service.TenantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
 * 租户管理Controller
 *
 * @author fanzh
 * @date 2019/5/22 22:52
 */
@Slf4j
@AllArgsConstructor
@Api("租户信息管理")
@RestController
@RequestMapping("/v1/tenant")
public class TenantController {

    private final TenantService tenantService;

    /**
     * 根据ID获取
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2019/05/22 22:53
     */
    @ApiOperation(value = "获取租户信息", notes = "根据租户id获取租户详细信息")
    @ApiImplicitParam(name = "id", value = "租户ID", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/{id}")
    public ExecResult<Tenant> tenant(@PathVariable Long id) {
        return ExecResultUtil.success(tenantService.baseGetById(id));
    }

    /**
     * 分页查询
     *
     * @param pageEntity
     * @param tenant
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = CommonConstant.PAGE_NUM, value = "分页页码", defaultValue = CommonConstant.PAGE_NUM_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.PAGE_SIZE, value = "分页大小", defaultValue = CommonConstant.PAGE_SIZE_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.SORT, value = "排序字段", defaultValue = CommonConstant.PAGE_SORT_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.ORDER, value = "排序方向", defaultValue = CommonConstant.PAGE_ORDER_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = "tenant", value = "租户信息", dataType = "Tenant")
    })
    @ApiOperation(value = "获取租户列表")
    @GetMapping("tenantList")
    public ExecResult<Page<Tenant>> tenantList(PageEntity pageEntity, Tenant tenant) {
        return ExecResultUtil.success(tenantService.baseListOrPage(OptionalUtil.build(pageEntity), tenant));
    }

    /**
     * 根据租户标识获取
     *
     * @param tenantCode tenantCode
     * @return ResponseBean
     * @author fanzh
     * @date 2019/05/26 10:23
     */
    @GetMapping("anonymousUser/findTenantByTenantCode/{tenantCode}")
    public ExecResult<Tenant> findTenantByTenantCode(@PathVariable String tenantCode) {
        return ExecResultUtil.success(tenantService.findByTenantCode(tenantCode));
    }

    /**
     * 创建租户
     *
     * @param tenant tenant
     * @return ResponseBean
     * @author fanzh
     * @date 2019/05/22 23:32
     */
    @ApiImplicitParam(name = "tenant", value = "租户实体tenant", required = true, dataType = "Tenant")
    @ApiOperation(value = "创建租户", notes = "创建租户")
    @PostMapping
    public ExecResult<Boolean> add(@RequestBody @Valid Tenant tenant) {
        // 初始化状态为待审核
        tenant.setStatus(TenantConstant.PENDING_AUDIT);
        // 保存租户
        tenantService.addTenant(tenant);
        return ExecResultUtil.success(true);
    }

    /**
     * 更新租户
     *
     * @param tenant tenant
     * @return ResponseBean
     * @author fanzh
     * @date 2019/05/22 23:33
     */
    @ApiImplicitParam(name = "tenant", value = "租户实体tenant", required = true, dataType = "Tenant")
    @ApiOperation(value = "更新租户信息", notes = "根据租户id更新租户的基本信息")
    @PutMapping
    public ExecResult<Boolean> update(@RequestBody @Valid Tenant tenant) {
        try {
            return new ExecResult<>(tenantService.updateTenant(tenant) > 0);
        } catch (java.lang.Exception e) {
            log.error("Update tenant failed", e);
        }
        return new ExecResult<>(Boolean.FALSE);
    }

    /**
     * 删除租户
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2019/05/22 23:35
     */
    @ApiImplicitParam(name = "id", value = "租户ID", required = true, paramType = "path")
    @ApiOperation(value = "删除租户", notes = "根据ID删除租户")
    @DeleteMapping("/{id}")
    public ExecResult<Boolean> delete(@PathVariable Long id) {
        tenantService.deleteTenant(SetUtil.build(id));
        return ExecResultUtil.success(true);
    }

    /**
     * 批量删除
     *
     * @param ids ids
     * @return ResponseBean
     * @author fanzh
     * @date 2019/05/22 23:37
     */
    @ApiImplicitParam(name = "ids", value = "租户ID", dataType = "Long")
    @ApiOperation(value = "批量删除租户", notes = "根据租户id批量删除租户")
    @PostMapping("deleteAll")
    public ExecResult<Boolean> deleteAll(@RequestBody List<Long> ids) {
        tenantService.deleteTenant(SetUtil.build(ids));
        return ExecResultUtil.success(true);
    }

    /**
     * 根据ID查询
     *
     * @param ids ids
     * @return ResponseBean
     * @author fanzh
     * @date 2019/05/22 23:38
     */
    @ApiOperation(value = "根据ID查询租户", notes = "根据ID查询租户")
    @ApiImplicitParam(name = "ids", value = "租户ID", required = true, paramType = "Long")
    @PostMapping("findById")
    public ExecResult<List<Tenant>> findById(@RequestBody List<Long> ids) {
        return ExecResultUtil.success(tenantService.baseFindById(SetUtil.build(ids)));
    }
}
