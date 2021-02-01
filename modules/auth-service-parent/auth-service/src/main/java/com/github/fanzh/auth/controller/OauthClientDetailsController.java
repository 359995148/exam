package com.github.fanzh.auth.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.auth.api.module.OauthClientDetails;
import com.github.fanzh.auth.service.OauthClientDetailsService;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.OptionalUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.common.security.annotations.AdminAuthorization;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Oauth2客户端信息管理
 *
 * @author fanzh
 * @date 2019/3/30 16:49
 */
@Slf4j
@AllArgsConstructor
@Api("Oauth2客户端信息管理")
@RestController
@RequestMapping("/v1/client")
public class OauthClientDetailsController {

    private final OauthClientDetailsService oauthClientDetailsService;

    @ApiOperation(value = "客户端查询")
    @GetMapping("/{id}")
    public ExecResult<OauthClientDetails> get(@PathVariable Long id) {
        return ExecResultUtil.success(oauthClientDetailsService.baseGetById(id));
    }

    @ApiOperation(value = "客户端列表或分页查询")
    @GetMapping("/list-page")
    public ExecResult<Page<OauthClientDetails>> listOrPage(
            @RequestParam(value = CommonConstant.PAGE_NUM, required = false, defaultValue = CommonConstant.PAGE_NUM_DEFAULT) @ApiParam(value = "页数") Integer pageNum
            , @RequestParam(value = CommonConstant.PAGE_SIZE, required = false, defaultValue = CommonConstant.PAGE_SIZE_DEFAULT) @ApiParam(value = "页长") Integer pageSize
            , @RequestParam(value = CommonConstant.SORT, required = false, defaultValue = CommonConstant.PAGE_SORT_DEFAULT) @ApiParam(value = "排序字段") String sort
            , @RequestParam(value = CommonConstant.ORDER, required = false, defaultValue = CommonConstant.PAGE_ORDER_DEFAULT) @ApiParam(value = "排序方式") String order
            , OauthClientDetails oauthClientDetails
    ) {
        return oauthClientDetailsService.listOrPage(
                OptionalUtil.build(pageNum)
                , OptionalUtil.build(pageSize)
                , OptionalUtil.build(sort)
                , OptionalUtil.build(order)
                , oauthClientDetails
        );
    }

    @AdminAuthorization
    @ApiOperation(value = "客户端新增")
    @PostMapping
    public ExecResult<Boolean> add(@RequestBody OauthClientDetails oauthClientDetails) {
        return oauthClientDetailsService.add(oauthClientDetails);
    }

    @AdminAuthorization
    @ApiOperation(value = "客户端更新")
    @PutMapping
    public ExecResult<Boolean> update(@RequestBody OauthClientDetails oauthClientDetails) {
        return oauthClientDetailsService.update(oauthClientDetails);
    }

    @AdminAuthorization
    @ApiOperation(value = "客户端删除")
    @DeleteMapping("/{id}")
    public ExecResult<Boolean> delete(@PathVariable Long id) {
        oauthClientDetailsService.baseLogicDelete(id);
        return ExecResultUtil.success(true);
    }

    @AdminAuthorization
    @ApiOperation(value = "客户端批量删除")
    @PostMapping("/list")
    public ExecResult<Boolean> listDelete(@RequestBody List<Long> ids) {
        oauthClientDetailsService.baseLogicDelete(SetUtil.build(ids));
        return ExecResultUtil.success(true);
    }
}
