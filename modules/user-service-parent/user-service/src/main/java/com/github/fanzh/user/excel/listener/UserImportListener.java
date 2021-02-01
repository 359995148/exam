package com.github.fanzh.user.excel.listener;

import com.github.fanzh.common.core.utils.excel.AbstractExcelImportListener;
import com.github.fanzh.user.service.UserService;
import com.github.fanzh.user.api.dto.UserInfoDto;
import com.github.fanzh.user.excel.model.UserExcelModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理导入用户数据
 * @author fanzh
 * @date 2019/12/10 16:34
 */
@Slf4j
public class UserImportListener extends AbstractExcelImportListener<UserExcelModel> {

	private UserService userService;

	public UserImportListener(UserService userService) {
		this.userService = userService;
	}

	/**
	 * 存储到数据库
	 */
	@Override
	public void saveData(List<UserExcelModel> userExcelModels) {
		log.info("SaveData size: {}", userExcelModels.size());
		List<UserInfoDto> userInfoDtoList = new ArrayList<>(userExcelModels.size());
		userExcelModels.forEach(data -> {
			UserInfoDto userInfoDto = new UserInfoDto();
			BeanUtils.copyProperties(data, userInfoDto);
			userInfoDtoList.add(userInfoDto);
		});
		userService.importUsers(userInfoDtoList);
	}
}
