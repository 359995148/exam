package com.github.fanzh.user.controller;

import com.github.fanzh.common.basic.vo.UserVo;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.exceptions.ServiceException;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.exam.api.dto.ExaminationDashboardDto;
import com.github.fanzh.exam.api.feign.ExaminationServiceClient;
import com.github.fanzh.user.api.dto.DashboardDto;
import com.github.fanzh.user.service.TenantService;
import com.github.fanzh.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台首页数据展示
 *
 * @author fanzh
 * @date 2019-03-01 13:54
 */
@AllArgsConstructor
@Api("后台首页数据展示")
@RestController
@RequestMapping("/v1/dashboard")
public class DashboardController {

    private final ExaminationServiceClient examinationService;

    private final UserService userService;

    private final TenantService tenantService;

    /**
     * 获取管控台首页数据
     *
     * @return ResponseBean
     * @author fanzh
     * @date 2019/3/1 13:55
     */
    @ApiOperation(value = "后台首页数据展示", notes = "后台首页数据展示")
    @GetMapping
    public ExecResult<DashboardDto> dashboard() {
        String tenantCode = SysUtil.getTenantCode();
        DashboardDto dashboardDto = new DashboardDto();
        // 查询用户数量
        UserVo userVo = new UserVo();
        userVo.setTenantCode(tenantCode);
        dashboardDto.setOnlineUserNumber(userService.userCount(userVo.getTenantCode()).toString());
        // 租户数量
        dashboardDto.setTenantCount(tenantService.tenantCount().toString());
        // 查询考试数量
        ExecResult<ExaminationDashboardDto> dashboardData = examinationService.findExaminationDashboardData(tenantCode);
        if (dashboardData.isError()) {
            throw new ServiceException("Get examination dashboard data failed: " + dashboardData.getMsg());
        }
        ExaminationDashboardDto examinationDashboardDto = dashboardData.getData();
        if (ParamsUtil.isEmpty(examinationDashboardDto)) {
            return ExecResultUtil.success(dashboardDto);
        }
        if (ParamsUtil.isNotEmpty(examinationDashboardDto.getExaminationCount())) {
            dashboardDto.setExaminationNumber(examinationDashboardDto.getExaminationCount().toString());
        }
        if (ParamsUtil.isNotEmpty(examinationDashboardDto.getExamUserCount())) {
            dashboardDto.setExamUserNumber(examinationDashboardDto.getExamUserCount().toString());
        }
        if (ParamsUtil.isNotEmpty(examinationDashboardDto.getExaminationRecordCount())) {
            dashboardDto.setExaminationRecordNumber(examinationDashboardDto.getExaminationRecordCount().toString());
        }
        return ExecResultUtil.success(dashboardDto);
    }

    /**
     * 过去一周考试记录数
     *
     * @return ResponseBean
     * @author fanzh
     * @date 2020/1/31 6:08 下午
     */
    @ApiOperation(value = "过去一周考试记录数", notes = "过去一周考试记录数")
    @GetMapping("examRecordTendency")
    public ExecResult<DashboardDto> examRecordTendency(@RequestParam Integer pastDays) {
        DashboardDto dashboardDto = new DashboardDto();
        ExecResult<ExaminationDashboardDto> examRecordTendencyData = examinationService.findExamRecordTendencyData(SysUtil.getTenantCode(), pastDays);
        if (examRecordTendencyData.isError()) {
            throw new ServiceException("Get examination record tendency data failed: " + examRecordTendencyData.getMsg());
        }
        dashboardDto.setExamRecordDate(examRecordTendencyData.getData().getExamRecordDate());
        dashboardDto.setExamRecordData(examRecordTendencyData.getData().getExamRecordData());
        return ExecResultUtil.success(dashboardDto);
    }
}
