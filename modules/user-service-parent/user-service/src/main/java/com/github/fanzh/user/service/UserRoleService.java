package com.github.fanzh.user.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.fanzh.common.core.constant.SqlField;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.basic.utils.EntityWrapperUtil;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.SetUtil;
import com.github.fanzh.user.mapper.UserRoleMapper;
import com.github.fanzh.user.api.module.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author fanzh
 * @date 2018/8/26 14:55
 */
@AllArgsConstructor
@Service
public class UserRoleService extends BaseService<UserRoleMapper, UserRole> {


    /**
     * 根据用户ID查询
     *
     * @param userId 用户ID
     * @return List
     */
    public List<UserRole> findByUserId(Long userId) {
        if (ParamsUtil.isEmpty(userId)) {
            return Collections.emptyList();
        }
        return findByUserId(SetUtil.build(userId));
    }

    /**
     * 根据用户ID查询
     *
     * @param userIds 用户ID
     * @return List
     */
    public List<UserRole> findByUserId(Set<Long> userIds) {
        if (ParamsUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        EntityWrapper<UserRole> ew = EntityWrapperUtil.build();
        ew.in(SqlField.USER_ID, userIds);
        return this.selectList(ew);
    }

}
