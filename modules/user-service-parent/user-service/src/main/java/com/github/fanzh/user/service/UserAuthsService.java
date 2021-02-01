package com.github.fanzh.user.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.fanzh.common.core.constant.SqlField;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.basic.utils.EntityWrapperUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.user.mapper.UserAuthsMapper;
import com.github.fanzh.user.api.module.UserAuths;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 用户授权Service
 *
 * @author fanzh
 * @date 2019/07/03 11:45
 */
@AllArgsConstructor
@Slf4j
@Service
public class UserAuthsService extends BaseService<UserAuthsMapper, UserAuths> {


    /**
     * 查询
     *
     * @param userId
     * @return
     */
    public List<UserAuths> findByUserId(Set<Long> userId) {
        if (ParamsUtil.isEmpty(userId)) {
            return Collections.emptyList();
        }
        EntityWrapper<UserAuths> ew = EntityWrapperUtil.build();
        ew.in(SqlField.USER_ID, userId);
        return selectList(ew);
    }

    /**
     * 查询
     *
     * @param identifier
     * @return
     */
    public List<UserAuths> findByIdentifier(String identifier) {
        return find(identifier, Optional.empty(), Optional.of(SysUtil.getTenantCode()));
    }

    /**
     * 查询
     *
     * @param identifier
     * @return
     */
    public Optional<UserAuths> findByIdentifier(String identifier, Integer identityType) {
        if (ParamsUtil.isEmpty(identityType)) {
            return Optional.empty();
        }
        return find(identifier, Optional.of(identityType), Optional.of(SysUtil.getTenantCode())).stream().findAny();
    }

    /**
     * 查询
     *
     * @param identifier
     * @return
     */
    public Optional<UserAuths> findByIdentifier(String identifier, Integer identityType, String tenantCode) {
        if (ParamsUtil.isEmpty(identifier) || ParamsUtil.isEmpty(identityType) || ParamsUtil.isEmpty(tenantCode)) {
            return Optional.empty();
        }
        return find(identifier, Optional.of(identityType), Optional.of(tenantCode)).stream().findAny();
    }

    /**
     * 查询
     *
     * @param identifier
     * @return
     */
    public List<UserAuths> find(String identifier, Optional<Integer> identityType, Optional<String> tenantCode) {
        if (ParamsUtil.isEmpty(identifier)) {
            return Collections.emptyList();
        }
        EntityWrapper<UserAuths> ew = EntityWrapperUtil.build();
        ew.eq("identifier", identifier);
        identityType.ifPresent(o -> ew.eq("identity_type", o));
        tenantCode.ifPresent(o -> ew.eq("tenant_code", o));
        return selectList(ew);
    }
}
