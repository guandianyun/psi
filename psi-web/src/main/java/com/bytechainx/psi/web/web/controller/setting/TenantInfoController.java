package com.bytechainx.psi.web.web.controller.setting;


import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.TenantOrg;
import com.bytechainx.psi.common.service.setting.TenantInfoService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Ret;


/**
* 商户信息
*/
@Path("/setting/tenant/info")
public class TenantInfoController extends BaseController {

	@Inject
	private TenantInfoService infoService;

	/**
	* 首页
	*/
	@Permission(Permissions.setting_tenant_info)
	public void index() {
		
	}
	
	/**
	* 列表
	*/
	@Permission(Permissions.setting_tenant_info)
	public void list() {
		setAttr("tenantInfo", TenantOrg.dao.findTop());
	}

	/**
	* 查看
	*/
	@Permission(Permissions.setting_tenant_info_show)
	public void show() {

		renderJson(Ret.ok());
	}


	/**
	* 编辑
	*/
	@Permission(Permissions.setting_tenant_info_update)
	public void edit() {
		setAttr("tenantInfo", TenantOrg.dao.findTop());
	}

	/**
	* 修改
	*/
	@Permission(Permissions.setting_tenant_info_update)
	public void update() {
		TenantOrg tenantOrg = getModel(TenantOrg.class, "", true);
		Ret ret = infoService.update(tenantOrg);
		renderJson(ret);
	}

}