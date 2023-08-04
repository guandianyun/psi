package com.bytechainx.psi.common.model;

import java.math.BigDecimal;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.FlagEnum;
import com.bytechainx.psi.common.EnumConstant.UserActiveStatusEnum;
import com.bytechainx.psi.common.model.base.BaseTenantAdmin;

/**
 * 租户员工表
 */
@SuppressWarnings("serial")
public class TenantAdmin extends BaseTenantAdmin<TenantAdmin> {
	public static final TenantAdmin dao = new TenantAdmin().dao();

	public TenantAdmin findMainAdmin() {
		return TenantAdmin.dao.findFirst("select * from tenant_admin where main_account_flag = ?", FlagEnum.YES.getValue());
	}
	
	/**
	 * 统计可登录用户数
	 * @param tenantOrgId
	 * @return
	 */
	public int countBy() {
		TenantAdmin admin = TenantAdmin.dao.findFirst("select count(*) as counts from tenant_admin where active_status = ? and login_flag = ?", UserActiveStatusEnum.enable.getValue(), FlagEnum.YES.getValue());
		return admin.getInt("counts");
	}
	
	public TenantAdmin findBy(String mobile) {
		return TenantAdmin.dao.findFirst("select * from tenant_admin where mobile = ?", mobile);
	}
	public TenantAdmin findByMobile(String mobile) {
		return TenantAdmin.dao.findFirst("select * from tenant_admin where mobile = ?", mobile);
	}
	public TenantAdmin findById(Integer id) {
		return TenantAdmin.dao.findFirst("select * from tenant_admin where id = ? limit 1", id);
	}
	
	public TenantRole getRole() {
		return TenantRole.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "tenant.role.id."+getRoleId(), "select * from tenant_role where id = ?", getRoleId());
	}
	
	public BigDecimal getCommissionAmount() {
		return getBigDecimal("commissionAmount");
	}
	
	/**
	 * 是否超级管理员
	 * @return
	 */
	public boolean isSuperAdmin() {
		if(getMainAccountFlag()) {
			return true;
		}
		if(getRole().getSuperFlag()) {
			return true;
		}
		return false;
	}
	
	
	public String getOnlineTimeStr() {
		int times = getOnlineTimes();
		if(times < 60) {
			return times+"分钟";
		}
		if(times/60 < 24) {
			return times/60+"小时"+times%60+"分钟";
		}
		return times/1440+"天"+ times/1440/60 + "小时"+times/1440%60+"分钟";
	}

	
}

