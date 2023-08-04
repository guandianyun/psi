package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.IndustryEnum;
import com.bytechainx.psi.common.EnumConstant.SaleModeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.model.base.BaseTenantOrg;

/**
 * 租户信息
 */
@SuppressWarnings("serial")
public class TenantOrg extends BaseTenantOrg<TenantOrg> {
	
	public static final TenantOrg dao = new TenantOrg().dao();
	
	public TenantOrg findCacheById() {
		return findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "tenant.org.id.1", "select * from tenant_org limit 1");
	}
	
	public String getModeName() {
		return SaleModeEnum.getEnum(getMode()).getName();
	}
	
	public String getIndustryName() {
		if(getIndustry() == null) {
			return null;
		}
		IndustryEnum industry = IndustryEnum.getEnum(getIndustry());
		if(industry == null) {
			return null;
		}
		return industry.getName();
	}
	
	public TenantConfig getConfig(TenantConfigEnum configEnum) {
		return TenantConfig.dao.findByKeyCache(configEnum);
	}
	
}

