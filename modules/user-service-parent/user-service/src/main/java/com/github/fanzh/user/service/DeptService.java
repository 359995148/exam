package com.github.fanzh.user.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.fanzh.common.core.constant.CommonConstant;
import com.github.fanzh.common.core.constant.SqlField;
import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.core.utils.TreeUtil;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.user.mapper.DeptMapper;
import com.github.fanzh.user.api.dto.DeptDto;
import com.github.fanzh.user.api.module.Dept;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门service
 *
 * @author fanzh
 * @date 2018/8/26 22:46
 */
@Service
public class DeptService extends BaseService<DeptMapper, Dept> {

	/**
	 * 查询树形部门集合
	 *
	 * @return List
	 * @author fanzh
	 * @date 2018/10/25 12:57
	 */
	public List<DeptDto> treeDept() {
		Dept dept = new Dept();
		dept.setApplicationCode(SysUtil.getSysCode());
		dept.setTenantCode(SysUtil.getTenantCode());
		// 查询部门集合
		EntityWrapper<Dept> ew = new EntityWrapper<>();
		ew.eq(SqlField.DEL_FLAG, CommonConstant.DEL_FLAG_NORMAL);
		ew.eq(SqlField.APPLICATION_CODE, SysUtil.getSysCode());
		ew.eq(SqlField.TENANT_CODE, SysUtil.getTenantCode());
		List<Dept> list = selectList(ew);
		if (ParamsUtil.isEmpty(list)) {
			return Collections.emptyList();
		}
		List<DeptDto> deptTreeList = list.stream().map(DeptDto::new).collect(Collectors.toList());
		// 排序、构建树形结构
		return TreeUtil.buildTree(CollUtil.sort(deptTreeList, Comparator.comparingInt(DeptDto::getSort)), CommonConstant.ROOT);
	}
}
