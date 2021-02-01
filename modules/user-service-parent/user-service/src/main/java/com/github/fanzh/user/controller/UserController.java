package com.github.fanzh.user.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.github.fanzh.common.core.utils.excel.ExcelToolUtil;
import com.github.fanzh.common.basic.vo.UserVo;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.entity.PageEntity;
import com.github.fanzh.common.core.utils.DateUtils;
import com.github.fanzh.common.basic.utils.EntityWrapperUtil;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.core.utils.JsonUtil;
import com.github.fanzh.common.core.utils.OptionalUtil;
import com.github.fanzh.common.basic.utils.PageUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.common.log.annotation.Log;
import com.github.fanzh.common.security.annotations.AdminTenantTeacherAuthorization;
import com.github.fanzh.common.security.constant.SecurityConstant;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.user.api.module.Dept;
import com.github.fanzh.user.api.module.Role;
import com.github.fanzh.user.api.module.User;
import com.github.fanzh.user.api.module.UserAuths;
import com.github.fanzh.user.api.module.UserRole;
import com.github.fanzh.user.excel.listener.UserImportListener;
import com.github.fanzh.user.service.DeptService;
import com.github.fanzh.user.service.RoleService;
import com.github.fanzh.user.service.UserAuthsService;
import com.github.fanzh.user.service.UserRoleService;
import com.github.fanzh.user.service.UserService;
import com.github.fanzh.user.api.dto.UserDto;
import com.github.fanzh.user.api.dto.UserInfoDto;
import com.github.fanzh.user.excel.model.UserExcelModel;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author fanzh
 * @date 2018-08-25 16:20
 */
@Slf4j
@AllArgsConstructor
@Api("用户信息管理")
@RestController
@RequestMapping(value = "/v1/user")
public class UserController {

    private final UserService userService;
    private final DeptService deptService;
    private final UserRoleService userRoleService;
    private final UserAuthsService userAuthsService;
    private final RoleService roleService;

    /**
     * 根据id获取
     *
     * @param id id
     * @return ResponseBean
     */
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "Long", paramType = "path")
    @ApiOperation(value = "获取用户信息", notes = "根据用户id获取用户详细信息")
    @GetMapping("/{id}")
    public ExecResult<User> user(@PathVariable Long id) {
        return ExecResultUtil.success(userService.baseGetById(id));
    }

    /**
     * 获取当前用户信息（角色、权限）
     *
     * @return 用户名
     */
    @ApiImplicitParam(name = "identityType", value = "账号类型", required = true, dataType = "String")
    @ApiOperation(value = "获取用户信息", notes = "获取当前登录用户详细信息")
    @GetMapping("info")
    public ExecResult<UserInfoDto> userInfo(
            @RequestParam(required = false) Integer identityType
            , OAuth2Authentication authentication
    ) {
        return ExecResultUtil.success(userService.findUserInfo(OptionalUtil.build(identityType), authentication.getName()));
    }

    /**
     * 根据用户唯一标识获取用户详细信息
     *
     * @param identifier   identifier
     * @param identityType identityType
     * @param tenantCode   tenantCode
     * @return ResponseBean
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "identifier", value = "用户唯一标识", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "identityType", value = "用户授权类型", dataType = "Integer"),
            @ApiImplicitParam(name = "tenantCode", value = "租户标识", required = true, dataType = "String"),
    })
    @ApiOperation(value = "获取用户信息", notes = "根据用户name获取用户详细信息")
    @GetMapping("anonymousUser/findUserByIdentifier/{identifier}")
    public ExecResult<UserVo> findUserByIdentifier(
            @PathVariable String identifier
            , @RequestParam(required = false) Integer identityType
            , @RequestParam @NotBlank String tenantCode
    ) {
        return new ExecResult<>(userService.findUserByIdentifier(identityType, identifier, tenantCode));
    }

    /**
     * 获取分页数据
     *
     * @param pageNum  pageNum
     * @param pageSize pageSize
     * @param sort     sort
     * @param order    order
     * @param userVo   userVo
     * @return PageInfo
     * @author fanzh
     * @date 2018/8/26 22:56
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = CommonConstant.PAGE_NUM, value = "分页页码", defaultValue = CommonConstant.PAGE_NUM_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.PAGE_SIZE, value = "分页大小", defaultValue = CommonConstant.PAGE_SIZE_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.SORT, value = "排序字段", defaultValue = CommonConstant.PAGE_SORT_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = CommonConstant.ORDER, value = "排序方向", defaultValue = CommonConstant.PAGE_ORDER_DEFAULT, dataType = "String"),
            @ApiImplicitParam(name = "userVo", value = "用户信息", dataType = "UserVo")
    })
    @ApiOperation(value = "获取用户列表")
    @GetMapping("userList")
    public ExecResult<Page<UserDto>> userList(PageEntity pageEntity, UserVo userVo) {
        User user = new User();
        BeanUtils.copyProperties(userVo, user);
        user.setTenantCode(SysUtil.getTenantCode());
        Page<User> page = userService.baseListOrPage(OptionalUtil.build(pageEntity), user);
        if (ParamsUtil.isEmpty(page.getRecords())) {
            return ExecResultUtil.success(PageUtil.copy(page));
        }
        List<User> users = page.getRecords();
        Set<Long> userId = users.stream().map(o -> o.getId()).collect(Collectors.toSet());
        List<UserAuths> userAuthsList = userAuthsService.findByUserId(userId);
        List<Dept> deptList = deptService.selectBatchIds(users.stream().map(o -> o.getDeptId()).collect(Collectors.toSet()));
        List<UserRole> userRoleList = userRoleService.findByUserId(userId);
        List<Role> roleList = roleService.baseFindById(userRoleList.stream().map(o -> o.getRoleId()).collect(Collectors.toSet()));

        List<UserDto> userDtoList = Lists.newArrayList();
        users.forEach(tempUser -> {
            UserDto userDto = new UserDto();
            userDtoList.add(userDto);
            BeanUtils.copyProperties(tempUser, userDto);
            //账号
            userAuthsList.stream().filter(tempUserAuths -> Objects.equals(tempUser.getId(), tempUserAuths.getUserId()))
                    .findAny().ifPresent(tempUserAuths -> userDto.setIdentifier(tempUserAuths.getIdentifier()));
            //部门
            deptList.stream()
                    .filter(tempDept -> tempDept.getId().equals(tempUser.getDeptId()))
                    .findAny().ifPresent(userDept -> {
                userDto.setDeptId(userDept.getId());
                userDto.setDeptName(userDept.getDeptName());
            });
            //角色
            List<Role> userRole = new ArrayList<>();
            userRoleList.stream()
                    .filter(tempUserRole -> tempUser.getId().equals(tempUserRole.getUserId()))
                    .forEach(tempUserRole -> roleList.stream()
                            .filter(role -> role.getId().equals(tempUserRole.getRoleId()))
                            .forEach(userRole::add));
            userDto.setRoleList(userRole);
        });
        Page<UserDto> pageDto = PageUtil.copy(page);
        pageDto.setRecords(userDtoList);
        return ExecResultUtil.success(pageDto);
    }

    /**
     * 创建用户
     *
     * @param userDto userDto
     * @return ResponseBean
     * @author fanzh
     * @date 2018/8/26 14:34
     */
    @ApiImplicitParam(name = "userDto", value = "用户实体user", required = true, dataType = "UserDto")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "创建用户", notes = "创建用户")
    @PostMapping
    public ExecResult<Boolean> addUser(@RequestBody @Valid UserDto userDto) {
        userService.createUser(userDto);
        return ExecResultUtil.success(true);
    }

    /**
     * 更新用户
     *
     * @param id      id
     * @param userDto userDto
     * @return ResponseBean
     * @author fanzh
     * @date 2018/8/26 15:06
     */
    @ApiImplicitParam(name = "userDto", value = "用户实体user", required = true, dataType = "UserDto")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "更新用户信息", notes = "根据用户id更新用户的基本信息、角色信息")
    @PutMapping("/{id:[a-zA-Z0-9,]+}")
    public ExecResult<Boolean> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        userDto.setId(id);
        userService.updateUser(userDto);
        return ExecResultUtil.success(true);
    }

    /**
     * 更新用户的基本信息
     *
     * @param userDto userDto
     * @return ResponseBean
     * @author fanzh
     * @date 2018/10/30 10:06
     */
    @ApiImplicitParam(name = "userDto", value = "用户实体user", required = true, dataType = "UserDto")
    @ApiOperation(value = "更新用户基本信息", notes = "根据用户id更新用户的基本信息")
    @PutMapping("updateInfo")
    public ExecResult<Boolean> updateInfo(@RequestBody UserDto userDto) {
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        userService.baseUpdate(user);
        return ExecResultUtil.success(true);
    }

    /**
     * 修改密码
     *
     * @param userDto userDto
     * @return ResponseBean
     * @author fanzh
     * @date 2019/06/21 20:09
     */
    @ApiImplicitParam(name = "userDto", value = "用户实体user", required = true, dataType = "UserDto")
    @ApiOperation(value = "修改用户密码", notes = "修改用户密码")
    @PutMapping("anonymousUser/updatePassword")
    public ExecResult<Boolean> updatePassword(@RequestBody UserDto userDto) {
        userService.updatePassword(userDto);
        return ExecResultUtil.success(true);
    }

    /**
     * 更新头像
     *
     * @param userDto userDto
     * @return ResponseBean
     * @author fanzh
     * @date 2019/06/21 18:08
     */
    @ApiImplicitParam(name = "userDto", value = "用户实体user", required = true, dataType = "UserDto")
    @ApiOperation(value = "更新用户头像", notes = "根据用户id更新用户的头像信息")
    @PutMapping("updateAvatar")
    public ExecResult<Boolean> updateAvatar(@RequestBody UserDto userDto) {
        userService.updateAvatar(userDto);
        return ExecResultUtil.success(true);
    }

    /**
     * 删除用户
     *
     * @param id id
     * @return ResponseBean
     * @author fanzh
     * @date 2018/8/26 15:28
     */
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, paramType = "path")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "删除用户", notes = "根据ID删除用户")
    @DeleteMapping("/{id}")
    public ExecResult<Boolean> deleteUser(@PathVariable Long id) {
        userService.deleteUser(SetUtil.build(id));
        return ExecResultUtil.success(true);
    }

    /**
     * 批量删除
     *
     * @param ids ids
     * @return ResponseBean
     * @author fanzh
     * @date 2018/12/4 9:58
     */
    @ApiImplicitParam(name = "ids", value = "用户信息", dataType = "Long")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "批量删除用户", notes = "根据用户id批量删除用户")
    @PostMapping("deleteAll")
    public ExecResult<Boolean> deleteAllUsers(@RequestBody List<Long> ids) {
        userService.deleteUser(SetUtil.build(ids));
        return ExecResultUtil.success(true);
    }

    /**
     * 导出
     *
     * @param ids ids
     * @author fanzh
     * @date 2018/11/26 22:11
     */
    @ApiImplicitParam(name = "userVo", value = "用户信息", required = true, dataType = "UserVo")
    @ApiOperation(value = "导出用户", notes = "根据用户id导出用户")
    @AdminTenantTeacherAuthorization
    @PostMapping("export")
    public void exportUser(@RequestBody List<Long> ids, HttpServletRequest request, HttpServletResponse response) {
        List<User> users;
        if (ParamsUtil.isNotEmpty(ids)) {
            users = userService.baseFindById(SetUtil.build(ids));
        } else {
            EntityWrapper<User> uEw = EntityWrapperUtil.build();
            users = userService.selectList(uEw);
        }
        List<UserAuths> userAuths = userAuthsService.findByUserId(users.stream().map(o -> o.getId()).collect(Collectors.toSet()));
        // 组装数据，转成dto
        List<UserInfoDto> userInfoDtos = users.stream().map(tempUser -> {
            UserInfoDto userDto = new UserInfoDto();
            userAuths.stream()
                    .filter(userAuth -> Objects.equals(userAuth.getUserId(), tempUser.getId()))
                    .findAny()
                    .ifPresent(userAuth -> {
                        BeanUtils.copyProperties(userAuths, userDto);
                        BeanUtils.copyProperties(tempUser, userDto);
                    });
            return userDto;
        }).collect(Collectors.toList());
        String fileName = "用户信息" + DateUtils.localDateMillisToString(LocalDateTime.now());
        List<UserExcelModel> userExcelModelList = JsonUtil.listToList(userInfoDtos, UserExcelModel.class);
        ExcelToolUtil.exportExcel(request, response, userExcelModelList, fileName, "sheet1", UserExcelModel.class);
    }

    /**
     * 导入数据
     *
     * @param file file
     * @return ResponseBean
     * @author fanzh
     * @date 2018/11/28 12:44
     */
    @PostMapping("import")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "导入数据", notes = "导入数据")
    @Log("导入用户")
    public ExecResult<Boolean> importUser(@ApiParam(value = "要上传的文件", required = true) MultipartFile file, HttpServletRequest request) {
        ExcelToolUtil.importExcel(file, UserExcelModel.class, new UserImportListener(userService));
        return ExecResultUtil.success(true);
    }


    /**
     * 根据ID查询
     *
     * @param ids ids
     * @return ResponseBean
     * @author fanzh
     * @date 2018/12/31 21:16
     */
    @ApiImplicitParam(name = "ids", value = "用户ID", required = true, paramType = "Long")
    @ApiOperation(value = "根据ID查询用户", notes = "根据ID查询用户")
    @PostMapping(value = "findById")
    public ExecResult<List<UserVo>> findById(@RequestBody List<Long> ids) {
        return ExecResultUtil.success(userService.findUserVoListById(SetUtil.build(ids)));
    }

    /**
     * 注册
     *
     * @param userDto userDto
     * @return ResponseBean
     * @author fanzh
     * @date 2019/01/10 22:35
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "grant_type", value = "授权类型（password、mobile）", required = true, defaultValue = "password", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "randomStr", value = "随机数", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "mobile", value = "手机号", dataType = "String", paramType = "query")
    })
    @ApiOperation(value = "注册", notes = "注册")
    @PostMapping("anonymousUser/register")
    public ExecResult<Boolean> register(@RequestBody @Valid UserDto userDto) {
        userService.register(userDto);
        return ExecResultUtil.success(true);
    }

    /**
     * 检查账号是否存在
     *
     * @param identityType identityType
     * @param identifier   identifier
     * @param tenantCode   tenantCode
     * @return ResponseBean
     * @author fanzh
     * @date 2019/04/23 15:35
     */
    @ApiOperation(value = "检查账号是否存在", notes = "检查账号是否存在")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "identityType", value = "用户唯一标识类型", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "identifier", value = "用户唯一标识", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "tenantCode", value = "租户标识", required = true, dataType = "String"),
    })
    @GetMapping("anonymousUser/checkExist/{identifier}")
    public ExecResult<Boolean> checkExist(
            @PathVariable("identifier") String identifier
            , @RequestParam Integer identityType
            , @RequestHeader(SecurityConstant.TENANT_CODE_HEADER) String tenantCode
    ) {
        return ExecResultUtil.success(userService.checkIdentifierIsExist(identifier, identityType, tenantCode));
    }

    /**
     * 查询用户数量
     *
     * @param userVo userVo
     * @return ResponseBean
     * @author fanzh
     * @date 2019/05/09 22:09
     */
    @PostMapping("userCount")
    public ExecResult<Integer> userCount(UserVo userVo) {
        return ExecResultUtil.success(userService.userCount(userVo.getTenantCode()));
    }

    /**
     * 重置密码
     *
     * @param userDto userDto
     * @return ResponseBean
     * @author fanzh
     * @date 2019/6/7 12:00
     */
    @ApiImplicitParam(name = "userDto", value = "用户实体user", required = true, dataType = "UserDto")
    @AdminTenantTeacherAuthorization
    @ApiOperation(value = "重置密码", notes = "根据用户id重置密码")
    @PutMapping("anonymousUser/resetPassword")
    public ExecResult<Boolean> resetPassword(@RequestBody UserDto userDto) {
        return userService.resetPassword(userDto.getIdentifier());
    }

    /**
     * 更新用户的基本信息
     *
     * @param userDto userDto
     * @return ResponseBean
     * @author fanzh
     * @date 2020/02/29 16:55
     */
    @ApiImplicitParam(name = "userDto", value = "用户实体user", required = true, dataType = "UserDto")
    @ApiOperation(value = "更新用户登录信息", notes = "根据用户id更新用户的登录信息")
    @PutMapping("anonymousUser/updateLoginInfo")
    public ExecResult<Boolean> updateLoginInfo(@RequestBody UserDto userDto) {
        Boolean success = false;
        Optional<UserAuths> opt = userAuthsService.findByIdentifier(userDto.getIdentifier()).stream().findAny();
        if (opt.isPresent()) {
            User user = new User();
            user.setId(opt.get().getUserId());
            user.setLoginTime(userDto.getLoginTime());
            userService.baseSave(user);
            success = true;
        }
        return new ExecResult<>(success);
    }
}
