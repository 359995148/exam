package com.github.fanzh.user.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.user.mapper.RoleMenuMapper;
import com.github.fanzh.user.api.module.RoleMenu;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fanzh
 * @date 2018/8/26 22:47
 */
@AllArgsConstructor
@Service
public class RoleMenuService extends BaseService<RoleMenuMapper, RoleMenu> {


    /**
     * 覆盖保存
     *
     * @param roleId
     * @param menuIds
     */
    @Transactional(rollbackFor = Throwable.class)
    public void put(Long roleId, List<Long> menuIds) {
        if (ParamsUtil.isEmpty(roleId)) {
            return;
        }
        EntityWrapper<RoleMenu> delEw = new EntityWrapper<>();
        delEw.eq("role_id", roleId);
        List<RoleMenu> list = new ArrayList<>();
        for (Long menuId : menuIds) {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            list.add(roleMenu);
        }
        this.baseSave(list);
    }
}
