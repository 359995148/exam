package com.github.fanzh.user.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.fanzh.common.core.enums.LoginTypeEnum;
import com.github.fanzh.common.core.properties.SysProperties;
import com.github.fanzh.common.basic.vo.UserVo;
import com.github.fanzh.common.core.constant.ApiMsg;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.constant.SqlField;
import com.github.fanzh.common.core.entity.ExecResult;
import com.github.fanzh.common.core.exceptions.CommonException;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.core.utils.AesUtil;
import com.github.fanzh.common.basic.utils.EntityWrapperUtil;
import com.github.fanzh.common.core.utils.ExecResultUtil;
import com.github.fanzh.common.basic.id.IdGen;
import com.github.fanzh.common.core.utils.OptionalUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.security.constant.SecurityConstant;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.user.api.enums.IdentityType;
import com.github.fanzh.user.api.constant.AttachmentConstant;
import com.github.fanzh.user.api.constant.MenuConstant;
import com.github.fanzh.user.api.constant.RoleConstant;
import com.github.fanzh.user.api.dto.UserDto;
import com.github.fanzh.user.api.dto.UserInfoDto;
import com.github.fanzh.user.api.module.Attachment;
import com.github.fanzh.user.api.module.Menu;
import com.github.fanzh.user.api.module.Role;
import com.github.fanzh.user.api.module.User;
import com.github.fanzh.user.api.module.UserAuths;
import com.github.fanzh.user.api.module.UserRole;
import com.github.fanzh.user.mapper.UserMapper;
import com.github.fanzh.user.utils.UserUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户service实现
 *
 * @author fanzh
 * @date 2018-08-25 16:17
 */
@AllArgsConstructor
@Slf4j
@Service
public class UserService extends BaseService<UserMapper, User> {

    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private final UserRoleService userRoleService;

    private final RoleService roleService;

    private final MenuService menuService;

    private final RedisTemplate redisTemplate;

    private final AttachmentService attachmentService;

    private final SysProperties sysProperties;

    private final UserAuthsService userAuthsService;


    /**
     * 获取用户信息，包括头像、角色、权限信息
     *
     * @param identityType
     * @param identifier
     * @return
     */
    public UserInfoDto findUserInfo(Optional<Integer> identityType, String identifier) {
        // 返回结果
        UserInfoDto userInfoDto = new UserInfoDto();
        // 根据唯一标识查询账号信息
        EntityWrapper<UserAuths> uaEw = EntityWrapperUtil.build();
        identityType.ifPresent(o -> uaEw.eq("identity_type", o));
        uaEw.eq("identifier", identifier);
        uaEw.eq(SqlField.TENANT_CODE, SysUtil.getTenantCode());
        UserAuths userAuths = userAuthsService.selectOne(uaEw);
        if (ParamsUtil.isEmpty(userAuths)) {
            throw new CommonException("Identifier " + identifier + "does not exist");
        }
        // 根据用户id查询用户详细信息
        User user = this.baseGetById(userAuths.getUserId());
        // 查询用户的角色信息
        List<Role> roles = roleService.findByUserId(userAuths.getUserId());
        // 根据角色查询权限
        List<String> permissions = this.getUserPermissions(user.getTenantCode(), user.getApplicationCode(), identifier, roles);
        userInfoDto.setRoles(roles.stream().map(Role::getRoleCode).toArray(String[]::new));
        userInfoDto.setPermissions(permissions.toArray(new String[0]));
        UserUtils.toUserInfoDto(userInfoDto, user, userAuths);
        // 头像信息
        userInfoDto.setAvatarUrl(getUserAvatar(OptionalUtil.build(user.getAvatarId())));
        return userInfoDto;
    }

    /**
     * 获取用户头像信息
     *
     * @author fanzh
     * @date 2019/06/21 17:49
     */
    private String getUserAvatar(Optional<Long> avatarId) {
        // 附件id不为空，获取对应的预览地址，否则获取配置默认头像地址
        String url = "";
        if (avatarId.isPresent()) {
            url = attachmentService.getPreviewUrl(avatarId.get());
        }
        if (ParamsUtil.isEmpty(url)) {
            url = sysProperties.getDefaultAvatar() + (new Random().nextInt(4) + 1) + ".jpg";
        }
        return url;
    }

    /**
     * 根据指定角色集合，查询用户权限数组
     *
     * @param tenantCode
     * @param applicationCode
     * @param identifier
     * @param roles
     * @return
     */
    public List<String> getUserPermissions(
            String tenantCode
            , String applicationCode
            , String identifier
            , List<Role> roles
    ) {
        // 用户权限
        List<Menu> menuList = new ArrayList<>();
        // 管理员
        if (UserUtils.isAdmin(identifier)) {
            menuList = menuService.find(
                    Optional.of(tenantCode)
                    , Optional.of(applicationCode)
                    , Optional.of(MenuConstant.MENU_TYPE_PERMISSION)
            );
        } else {
            for (Role role : roles) {
                // 根据角色查找菜单
                menuList.addAll(menuService.findMenuByRole(role.getRoleCode(), tenantCode));
            }
        }
        List<String> permissions = menuList.stream()
                // 获取权限菜单
                .filter(menu -> MenuConstant.MENU_TYPE_PERMISSION.equals(menu.getType()))
                // 获取权限
                .map(o -> o.getPermission()).collect(Collectors.toList());
        ;
        return permissions;
    }

    /**
     * 根据用户唯一标识获取用户详细信息
     *
     * @param identityType identityType
     * @param identifier   identifier
     * @param tenantCode   tenantCode
     * @return UserVo
     * @author fanzh
     * @date 2019/07/03 13:00:39
     */
    public UserVo findUserByIdentifier(Integer identityType, String identifier, String tenantCode) {
        EntityWrapper<UserAuths> uaEw = new EntityWrapper<>();
        uaEw.eq(SqlField.DEL_FLAG, CommonConstant.DEL_FLAG_NORMAL);
        uaEw.eq("identifier", identifier);
        if (ParamsUtil.isNotEmpty(identityType)) {
            uaEw.eq("identity_type", IdentityType.matchByType(identityType).getValue());
        }
        uaEw.eq(SqlField.TENANT_CODE, tenantCode);
        UserAuths userAuths = userAuthsService.selectOne(uaEw);
        if (userAuths == null) {
            return null;
        }
        // 查询用户信息
        User user = this.selectById(userAuths.getUserId());
        if (user == null) {
            return null;
        }
        // 查询用户角色
        List<Role> roles = roleService.findByUserId(user.getId());
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        BeanUtils.copyProperties(userAuths, userVo);
        userVo.setRoleList(UserUtils.rolesToVo(roles));
        userVo.setUserId(user.getId());
        return userVo;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void createUser(UserDto userDto) {
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        // 保存父子账号关系
        UserVo currentUser = this.findUserByIdentifier(userDto.getIdentityType(), SysUtil.getUser(), SysUtil.getTenantCode());
        user.setParentUid(currentUser.getId());
        this.baseSave(user);
        initRoleAndAuth(
                user.getId()
                , userDto.getIdentifier()
                , OptionalUtil.build(userDto.getIdentityType())
                , OptionalUtil.build(userDto.getCredential())
                , OptionalUtil.build(userDto.getRole())
        );
    }

    /**
     * 初始化角色以及账号
     *
     * @param userId
     * @param identifier
     * @param identityType
     * @param credential
     * @param roleIds
     */
    private void initRoleAndAuth(
            Long userId
            , String identifier
            , Optional<Integer> identityType
            , Optional<String> credential
            , Optional<List<Long>> roleIds
    ) {
        // 分配默认角色
        List<UserRole> userRoleList = new ArrayList<>();
        EntityWrapper<Role> rEw = EntityWrapperUtil.build();
        rEw.eq("is_default", RoleConstant.IS_DEFAULT_ROLE);
        rEw.eq(SqlField.TENANT_CODE, SysUtil.getTenantCode());
        List<Long> roleId = roleIds.orElse(Collections.emptyList());
        Optional<Role> roleOpt = roleService.selectList(rEw).stream().filter(o -> !roleId.contains(o.getId())).findAny();
        if (roleOpt.isPresent()) {
            Role defaultRole = roleOpt.get();
            UserRole userRole = new UserRole();
            userRoleList.add(userRole);
            userRole.setUserId(userId);
            userRole.setRoleId(defaultRole.getId());
        }
        for (Long tempRoleId : roleId) {
            UserRole sysUserRole = new UserRole();
            sysUserRole.setUserId(userId);
            sysUserRole.setRoleId(tempRoleId);
            userRoleList.add(sysUserRole);
        }
        // 保存角色
        userRoleService.baseSave(userRoleList);
        // 保存用户授权信息
        UserAuths userAuths = new UserAuths();
        userAuths.setUserId(userId);
        userAuths.setIdentifier(identifier);
        userAuths.setIdentityType(identityType.orElse(IdentityType.PASSWORD.getValue()));
        userAuths.setCredential(encoder.encode(credential.orElse(CommonConstant.DEFAULT_PASSWORD)));
        userAuthsService.baseSave(userAuths);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateUser(UserDto userDto) {
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        // 更新用户信息
        this.baseUpdate(user);
        // 删除原有的角色信息
        userRoleService.baseDelete(userRoleService.findByUserId(user.getId()).stream().map(o -> o.getId()).collect(Collectors.toSet()));
        if (CollectionUtils.isNotEmpty(userDto.getRole())) {
            List<UserRole> userRoleList = new ArrayList<>();
            userDto.getRole().forEach(roleId -> {
                UserRole userRole = new UserRole();
                userRoleList.add(userRole);
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
            });
            // 保存角色信息
            userRoleService.baseSave(userRoleList);
        }
    }

    /**
     * 更新密码
     *
     * @param userDto userDto
     * @return int
     * @author fanzh
     * @date 2019/07/03 12:26:24
     */
    @CacheEvict(value = "user", key = "#userDto.identifier")
    @Transactional(rollbackFor = Throwable.class)
    public void updatePassword(UserDto userDto) {
        if (ParamsUtil.isEmpty(userDto.getNewPassword())) {
            throw new CommonException("New password cannot be empty");
        }
        if (ParamsUtil.isEmpty(userDto.getIdentifier())) {
            throw new CommonException("Identifier cannot be empty");
        }
        userDto.setTenantCode(SysUtil.getTenantCode());
        Optional<UserAuths> userAuthsOpt = userAuthsService.findByIdentifier(userDto.getIdentifier(), IdentityType.PASSWORD.getValue());
        if (!userAuthsOpt.isPresent()) {
            throw new CommonException("UserAuths does not exist by identifier: " + userDto.getIdentifier());
        }
        if (!encoder.matches(userDto.getOldPassword(), userAuthsOpt.get().getCredential())) {
            throw new CommonException("The old and new passwords do not match");
        }
        UserAuths userAuths = userAuthsOpt.get();
        // 新旧密码一致，修改密码
        userAuths.setCredential(encoder.encode(userDto.getNewPassword()));
        userAuthsService.baseSave(userAuths);
    }

    /**
     * 更新头像
     *
     * @param userDto userDto
     * @return int
     * @author fanzh
     * @date 2019/06/21 18:14
     */
    @CacheEvict(value = "user", key = "#userDto.identifier")
    @Transactional(rollbackFor = Throwable.class)
    public void updateAvatar(UserDto userDto) {
        User user = this.baseGetById(userDto.getId());
        // 先删除旧头像
        attachmentService.baseLogicDelete(user.getAvatarId());
        user.setAvatarId(userDto.getAvatarId());
        this.baseSave(user);
    }

    /**
     * 批量删除用户
     *
     * @param ids ids
     * @return int
     * @author fanzh
     * @date 2019/07/04 11:44:45
     */
    @CacheEvict(value = "user", allEntries = true)
    @Transactional(rollbackFor = Throwable.class)
    public void deleteUser(Set<Long> ids) {
        // 删除用户角色关系
        List<UserRole> userRoleList = userRoleService.findByUserId(ids);
        // 删除用户授权信息
        List<UserAuths> userAuthsList = userAuthsService.findByUserId(ids);
        userRoleService.baseLogicDelete(userRoleList.stream().map(o -> o.getId()).collect(Collectors.toSet()));
        userAuthsService.baseLogicDelete(userAuthsList.stream().map(o -> o.getId()).collect(Collectors.toSet()));
        this.baseLogicDelete(ids);
    }

    /**
     * 导入用户
     *
     * @param userInfoDtos userInfoDtos
     * @return boolean
     * @author fanzh
     * @date 2019/07/04 12:46:01
     */
    @CacheEvict(value = "user", allEntries = true)
    @Transactional(rollbackFor = Throwable.class)
    public void importUsers(List<UserInfoDto> userInfoDtos) {
        List<User> userList = new ArrayList<>();
        Set<Long> delUserAuthsIds = new HashSet<>();
        List<UserAuths> userAuthsList = new ArrayList<>();
        for (UserInfoDto userInfoDto : userInfoDtos) {
            User user = new User();
            BeanUtils.copyProperties(userInfoDto, user);
            if (ParamsUtil.isEmpty(user.getId())) {
                user.setId(IdGen.snowflakeId());
            }
            userList.add(user);
            // 先删除用户授权信息
            List<UserAuths> userAuthsOpt = userAuthsService.findByIdentifier(userInfoDto.getIdentifier());
            if (ParamsUtil.isNotEmpty(userAuthsOpt)) {
                delUserAuthsIds.addAll(userAuthsOpt.stream().map(o -> o.getId()).collect(Collectors.toSet()));
            }
            //新增授权信息
            UserAuths userAuths = new UserAuths();
            userAuths.setIdentifier(userInfoDto.getIdentifier());
            // 默认密码
            if (StringUtils.isBlank(userInfoDto.getCredential())) {
                userInfoDto.setCredential(encoder.encode(CommonConstant.DEFAULT_PASSWORD));
            }
            userAuths.setCredential(userInfoDto.getCredential());
            userAuths.setUserId(user.getId());
            userAuths.setIdentityType(userInfoDto.getIdentityType());
            userAuthsList.add(userAuths);
        }
        this.baseSave(userList);
        userAuthsService.baseDelete(delUserAuthsIds);
        userAuthsService.baseSave(userAuthsList);
    }

    /**
     * 根据用户id批量查询UserVo
     *
     * @param ids ids
     * @return List
     * @author fanzh
     * @date 2019/07/03 13:59:32
     */
    public List<UserVo> findUserVoListById(Set<Long> ids) {
        List<User> userList = this.baseFindById(ids);
        if (ParamsUtil.isEmpty(userList)) {
            return Collections.emptyList();
        }
        List<UserVo> userVos = userList.stream().map(tempUser -> {
            UserVo tempUserVo = new UserVo();
            BeanUtils.copyProperties(tempUser, tempUserVo);
            tempUserVo.setAvatarUrl(attachmentService.getPreviewUrl(tempUser.getAvatarId()));
            return tempUserVo;
        }).collect(Collectors.toList());
        return userVos;
    }

    /**
     * 注册，注意要清除缓存
     *
     * @param userDto userDto
     * @return boolean
     * @author fanzh
     * @date 2019/07/03 13:30:03
     */
    @CacheEvict(value = "user", key = "#userDto.identifier")
    @Transactional(rollbackFor = Throwable.class)
    public void register(UserDto userDto) {
        if (ParamsUtil.isEmpty(userDto.getIdentityType())) {
            userDto.setIdentityType(IdentityType.PASSWORD.getValue());
        }
        // 解密
        String password = this.decryptCredential(userDto.getCredential(), userDto.getIdentityType());
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        // 初始化用户名
        user.setCreator(userDto.getIdentifier());
        user.setModifier(userDto.getIdentifier());
        user.setStatus(CommonConstant.STATUS_NORMAL);
        // 初始化头像
        if (StringUtils.isNotBlank(userDto.getAvatarUrl())) {
            Attachment attachment = new Attachment();
            attachment.setCreator(userDto.getIdentifier());
            attachment.setModifier(userDto.getIdentifier());
            attachment.setBusiType(AttachmentConstant.BUSI_TYPE_USER_AVATAR);
            attachment.setPreviewUrl(userDto.getAvatarUrl());
            attachmentService.baseSave(attachment);
            user.setAvatarId(attachment.getId());
        }
        this.baseSave(user);
        // 初始化用户角色以及账号
        initRoleAndAuth(
                user.getId()
                , userDto.getIdentifier()
                , OptionalUtil.build(userDto.getIdentityType())
                , OptionalUtil.build(password)
                , OptionalUtil.build(userDto.getRole())
        );

    }

    /**
     * 解密密码
     *
     * @param encoded encoded
     * @return String
     * @author fanzh
     * @date 2019/07/05 12:39:13
     */
    private String decryptCredential(String encoded, Integer identityType) {
        // 返回默认密码
        if (StringUtils.isBlank(encoded)) {
            return CommonConstant.DEFAULT_PASSWORD;
        }
        // 微信、手机号注册不需要解密
        if (IdentityType.WE_CHAT.getValue().equals(identityType) || IdentityType.PHONE_NUMBER.getValue().equals(identityType)) {
            return encoded;
        }
        // 解密密码
        try {
            encoded = AesUtil.decryptAES(encoded, sysProperties.getKey()).trim();
            log.info("Decrypt result: {}", encoded);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CommonException("Decrypt failed: " + e.getMessage());
        }
        return encoded;
    }

    /**
     * 查询账号是否存在
     *
     * @param identityType identityType
     * @param identifier   identifier
     * @param tenantCode   tenantCode
     * @return boolean
     * @author fanzh
     * @date 2019/07/03 13:23:10
     */
    public boolean checkIdentifierIsExist(String identifier, Integer identityType, String tenantCode) {
        return userAuthsService.findByIdentifier(identifier, identityType, tenantCode).isPresent();
    }

    /**
     * 查询用户数量
     *
     * @param tenantCode
     * @return
     */
    public Integer userCount(String tenantCode) {
        EntityWrapper<User> ew = EntityWrapperUtil.build();
        ew.eq(SqlField.TENANT_CODE, tenantCode);
        return this.selectCount(ew);
    }

    /**
     * 重置密码
     *
     * @param identifier
     * @return
     */
    @CacheEvict(value = "user", key = "#userDto.identifier")
    @Transactional(rollbackFor = Throwable.class)
    public ExecResult<Boolean> resetPassword(String identifier) {
        if (ParamsUtil.isEmpty(identifier)) {
            return ExecResultUtil.error(ApiMsg.KEY_PARAM_VALIDATE, "Param identifier cannot be empty");
        }
        Optional<UserAuths> userAuthsOpt = userAuthsService.findByIdentifier(identifier, IdentityType.PASSWORD.getValue());
        if (!userAuthsOpt.isPresent()) {
            return ExecResultUtil.error(ApiMsg.KEY_PARAM_VALIDATE, "Param identifier is invalid");
        }
        UserAuths userAuths = userAuthsOpt.get();
        // 重置密码为123456
        userAuths.setCredential(encoder.encode(CommonConstant.DEFAULT_PASSWORD));
        userAuthsService.baseSave(userAuths);
        return ExecResultUtil.success(true);
    }

    /**
     * 保存验证码
     *
     * @param random    random
     * @param imageCode imageCode
     * @author fanzh
     * @date 2018/9/14 20:12
     */
    public void saveImageCode(String random, String imageCode) {
        redisTemplate.opsForValue().set(CommonConstant.DEFAULT_CODE_KEY + LoginTypeEnum.PWD.getType() + "@" + random, imageCode, SecurityConstant.DEFAULT_IMAGE_EXPIRE, TimeUnit.SECONDS);
    }
}
