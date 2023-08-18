package com.bytechainx.psi.web.web.controller.setting;


import java.util.Date;

import com.bytechainx.psi.common.EnumConstant.FlagEnum;
import com.bytechainx.psi.common.EnumConstant.UserActiveStatusEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.kit.CipherkeyUtil;
import com.bytechainx.psi.common.kit.PinYinUtil;
import com.bytechainx.psi.common.kit.RandomUtil;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TenantOrg;
import com.bytechainx.psi.common.model.TenantRole;
import com.bytechainx.psi.common.service.setting.SystemResetService;
import com.bytechainx.psi.common.service.setting.TenantInfoService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Clear;
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
	@Inject
	private SystemResetService systemResetService;

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
	
	/**
	 * 创建商户
	 * @param kv
	 * @return
	 */
	@Clear
	public void create() {
		TenantAdmin tenantAdmin = TenantAdmin.dao.findFirst("select * from tenant_admin where mobile = ? and active_status = ?", getPara("mobile"), UserActiveStatusEnum.enable.getValue());
		if(tenantAdmin != null) {
			renderJson(Ret.fail("租户手机号已存在，不能重复创建"));
			return;
		}
		TenantOrg tenantOrg = TenantOrg.dao.findCacheById();
		if(tenantOrg != null) {
			renderJson(Ret.fail("租户urlCode已存在，不能重复"));
			return;
		}
		tenantOrg = new TenantOrg();
		tenantOrg.setFullName(getPara("fullName"));
		tenantOrg.setCreatedAt(new Date());
		tenantOrg.setAddress(getPara("address"));
		tenantOrg.setCorporate(getPara("corporate"));
		tenantOrg.setDomain(getPara("domain"));
		tenantOrg.setIndustry(getParaToInt("industry"));
		tenantOrg.setLicense(getPara("license"));
		tenantOrg.setLogo(getPara("logo"));
		tenantOrg.setMobile(getPara("mobile"));
		tenantOrg.setMode(getParaToInt("mode"));
		tenantOrg.setName(getPara("name"));
		tenantOrg.setUpdatedAt(new Date());
		tenantOrg.save();
		
		// 创建默认角色
		TenantRole tenantRole = new TenantRole();
		tenantRole.setCreatedAt(new Date());
		tenantRole.setSuperFlag(FlagEnum.YES.getValue());
		tenantRole.setName("超级管理员");
		tenantRole.setDefaultFlag(FlagEnum.YES.getValue());
		tenantRole.setUpdatedAt(new Date());
		tenantRole.save();

		// 创建主账户
		TenantAdmin admin = new TenantAdmin();
		admin.setActiveStatus(UserActiveStatusEnum.enable.getValue());
		admin.setCreatedAt(new Date());
		admin.setEncrypt(CipherkeyUtil.encodeSalt(RandomUtil.genRandomNum(10)));
		admin.setPassword(CipherkeyUtil.encodePassword("667788", admin.getEncrypt()));
		admin.setLoginFlag(FlagEnum.YES.getValue());
		admin.setMainAccountFlag(FlagEnum.YES.getValue());
		admin.setMobile(tenantOrg.getMobile());
		admin.setRealName("主帐号");
		admin.setCode(PinYinUtil.getFirstSpell(admin.getRealName()));
		admin.setRoleId(tenantRole.getId());
		admin.setUpdatedAt(new Date());
		admin.save();
		
		Ret ret = systemResetService.initTenantData();
		
		renderJson(ret);
	}

}