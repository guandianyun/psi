package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.model.base.BaseTenantRole;
import com.jfinal.plugin.activerecord.Db;

/**
 * 角色
 */
@SuppressWarnings("serial")
public class TenantRole extends BaseTenantRole<TenantRole> {
	
	public static final TenantRole dao = new TenantRole().dao();
	
	
	public TenantRole findById(Integer id) {
		return TenantRole.dao.findFirst("select * from tenant_role where id = ? limit 1", id);
	}
	
	/**
	 * 计算角色所有的员工数量
	 * @return
	 */
	public int getCountAdmin() {
		return Db.queryInt("select count(*) from tenant_admin where role_id = ?", getId());
	}
	
	public boolean hasOper(String operCode) {
		if(getSuperFlag()){
			return true;
		}
		TenantRoleOperRef operRef = TenantRoleOperRef.dao.findFirst("select * from tenant_role_oper_ref where role_id = ? and oper_code = ?", getId(), operCode);
		return operRef != null ? true : false;
	}
	
	private String[] operCodes; // 权限操作code，用于值传递

	public String[] getOperCodes() {
		return operCodes;
	}

	public void setOperCodes(String[] operCodes) {
		this.operCodes = operCodes;
	}

}

