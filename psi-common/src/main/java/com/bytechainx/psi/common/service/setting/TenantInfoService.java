package com.bytechainx.psi.common.service.setting;


import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.model.TenantOrg;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Ret;


/**
* 商户信息
*/
public class TenantInfoService extends CommonService {

	/**
	* 修改
	*/
	public Ret update(TenantOrg tenantOrg) {
		if(tenantOrg == null) {
			return Ret.fail("参数错误");
		}
		if(StringUtils.isEmpty(tenantOrg.getName())) {
			return Ret.fail("公司简称不能为空");
		}
		if(StringUtils.isEmpty(tenantOrg.getMobile())) {
			return Ret.fail("服务电话不能为空");
		}
		tenantOrg.setUpdatedAt(new Date());
		tenantOrg.update();
		
		return Ret.ok();
	}
	

}