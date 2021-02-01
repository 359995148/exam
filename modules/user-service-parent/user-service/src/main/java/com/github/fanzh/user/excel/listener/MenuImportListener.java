package com.github.fanzh.user.excel.listener;

import com.github.fanzh.common.core.utils.excel.AbstractExcelImportListener;
import com.github.fanzh.common.core.utils.JsonUtil;
import com.github.fanzh.user.api.module.Menu;
import com.github.fanzh.user.service.MenuService;
import com.github.fanzh.user.excel.model.MenuExcelModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 菜单导入
 *
 * @author fanzh
 * @date 2019/12/10 17:22
 */
@Slf4j
public class MenuImportListener extends AbstractExcelImportListener<MenuExcelModel> {

    private MenuService menuService;

    public MenuImportListener(MenuService menuService) {
        this.menuService = menuService;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void saveData(List<MenuExcelModel> menuExcelModels) {
        log.info("SaveData size: {}", menuExcelModels.size());
        List<Menu> menuList = JsonUtil.listToList(menuExcelModels, Menu.class);
        menuService.baseSave(menuList);
    }
}
