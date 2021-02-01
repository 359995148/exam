package com.github.fanzh.user.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.entity.PageEntity;
import com.github.fanzh.common.core.model.Log;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.utils.OptionalUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.common.security.annotations.AdminAuthorization;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.user.service.LogService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 日志controller
 *
 * @author fanzh
 * @date 2018/10/31 20:48
 */
@Slf4j
@AllArgsConstructor
@Api("日志信息管理")
@RestController
@RequestMapping("/v1/log")
public class LogController {

    private final LogService logService;


    @ApiImplicitParam(name = "id", value = "日志ID", required = true, dataType = "Long", paramType = "path")
    @ApiOperation(value = "获取日志信息", notes = "根据日志id获取日志详细信息")
    @GetMapping("/{id}")
    public Log log(@PathVariable Long id) {
        return logService.selectById(id);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = CommonConstant.PAGE_NUM, value = "分页页码", defaultValue = CommonConstant.PAGE_NUM_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.PAGE_SIZE, value = "分页大小", defaultValue = CommonConstant.PAGE_SIZE_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.SORT, value = "排序字段", defaultValue = CommonConstant.PAGE_SORT_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.ORDER, value = "排序方向", defaultValue = CommonConstant.PAGE_ORDER_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = "log", value = "日志信息", dataType = "Log")
    })
    @ApiOperation(value = "获取日志列表")
    @GetMapping("logList")
    public ExecResult<Page<Log>> userList(PageEntity pageEntity, Log log) {
        log.setTenantCode(SysUtil.getTenantCode());
        return ExecResultUtil.success(logService.baseListOrPage(OptionalUtil.build(pageEntity), log));
    }

    @ApiImplicitParam(name = "log", value = "日志实体Log", required = true, dataType = "Log")
    @ApiOperation(value = "新增日志", notes = "新增日志")
    @PostMapping
    public ExecResult<Boolean> addLog(@RequestBody @Valid Log log) {
        // 保存日志
        logService.baseSave(log);
        return ExecResultUtil.success(true);
    }

    /**
     * 删除日志
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2018/10/31 21:27
     */
    @ApiImplicitParam(name = "id", value = "日志ID", required = true, paramType = "path")
    @AdminAuthorization
    @ApiOperation(value = "删除日志", notes = "根据ID删除日志")
    @DeleteMapping("/{id}")
    public ExecResult<Boolean> delete(@PathVariable Long id) {
        logService.baseLogicDelete(id);
        return ExecResultUtil.success(true);
    }

    /**
     * 批量删除
     *
     * @param ids ids
     * @return ResponseBean
     * @author fanzh
     * @date 2018/12/4 10:12
     */
    @ApiImplicitParam(name = "ids", value = "日志ID", dataType = "Long")
    @AdminAuthorization
    @ApiOperation(value = "批量删除日志", notes = "根据日志ids批量删除日志")
    @PostMapping("deleteAll")
    public ExecResult<Boolean> deleteAllLog(@RequestBody List<Long> ids) {
        logService.baseLogicDelete(SetUtil.build(ids));
        return ExecResultUtil.success(true);
    }
}
