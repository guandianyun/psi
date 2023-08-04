package com.bytechainx.psi.common.model;

import java.util.ArrayList;
import java.util.List;

import com.bytechainx.psi.common.EnumConstant.OperTypeEnum;
import com.bytechainx.psi.common.model.base.BaseSystemOper;

/**
 * 权限点
 */
@SuppressWarnings("serial")
public class SystemOper extends BaseSystemOper<SystemOper> {
	
	public static final SystemOper dao = new SystemOper().dao();
	
	/**
	 * 根据CODE查询权限点
	 * @param operCode
	 * @return
	 */
	public SystemOper findByOperCode(String operCode) {
		return SystemOper.dao.findFirst("select * from system_oper where oper_code = ? limit 1", operCode);
	}
	
	/**
	 * 获取子权限
	 * @param parent
	 * @return
	 */
	public List<SystemOper> getChildList() {
		List<SystemOper> operList = SystemOper.dao.find("select * from system_oper where parent_id = ?", getId());
		return filterOperList(operList);
	}
	
	/**
	 * 获取第三级权限数量
	 * @param parent
	 * @return
	 */
	public int getThirdCount() {
		int thirdSize = 0;
		List<SystemOper> childList = getChildList();
		for (SystemOper c : childList) {
			List<SystemOper> thirdList = c.getChildList();
			thirdSize += thirdList.size();
		}
		return thirdSize;
	}

	/**
	 * 获取数据顶级权限
	 * @param integer 
	 * @param parent
	 * @return
	 */
	public List<SystemOper> findDataTopList() {
		List<SystemOper> operList = SystemOper.dao.find("select * from system_oper where parent_id = ? and oper_type = ?", 0, OperTypeEnum.data.getValue());
		return filterOperList(operList);
	}

	/**
	 * 获取功能顶级权限
	 * @param parent
	 * @return
	 */
	public List<SystemOper> findFeatureTopList() {
		List<SystemOper> operList = SystemOper.dao.find("select * from system_oper where parent_id = ? and oper_type = ?", 0, OperTypeEnum.feature.getValue());
		return filterOperList(operList);
	}
	
	/**
	 * 根据是否开通模块，过滤权限
	 * @param tenantOrgId
	 * @param operList
	 * @return
	 */
	private List<SystemOper> filterOperList(List<SystemOper> operList) {
		List<SystemOper> _operList = new ArrayList<>();
		for (SystemOper oper : operList) {
			_operList.add(oper);
		}
		return _operList;
	}
}

