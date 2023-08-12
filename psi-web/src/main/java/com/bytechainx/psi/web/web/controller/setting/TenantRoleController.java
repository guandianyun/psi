package com.bytechainx.psi.web.web.controller.setting;


import java.util.Arrays;
import java.util.List;

import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.SystemOper;
import com.bytechainx.psi.common.model.TenantRole;
import com.bytechainx.psi.common.service.setting.TenantRoleService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 角色管理
*/
@Path("/setting/tenant/role")
public class TenantRoleController extends BaseController {
	
	@Inject
	private TenantRoleService roleService;
	
	/**
	* 首页
	*/
	@Permission(Permissions.setting_tenant_role)
	public void index() {
		
	}

	/**
	* 列表
	*/
	@Permission(Permissions.setting_tenant_role_show)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Page<TenantRole> page = roleService.paginate(pageNumber, pageSize);
		setAttr("page", page);
	}

	/**
	* 查看
	*/
	@Permission(Permissions.setting_tenant_role_show)
	public void show() {

		renderJson(Ret.ok());
	}


	/**
	* 添加
	*/
	@Permission(Permissions.setting_tenant_role_create)
	public void add() {
		setAttrCommon();
	}

	/**
	* 修改
	*/
	@Permission(Permissions.setting_tenant_role_create)
	@Before(Tx.class)
	public void create() {
		TenantRole role = getModel(TenantRole.class, "", true);
		role.setOperCodes(getParaValues("operCodes"));
		Ret ret = roleService.create(role);
		
		renderJson(ret);
	}



	/**
	* 编辑
	*/
	@Permission(Permissions.setting_tenant_role_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TenantRole role = TenantRole.dao.findById(id);
		if(role == null) {
			renderError(404);
			return;
		}
		setAttrCommon();
		setAttr("tenantRole", role);

	}

	/**
	* 修改
	*/
	@Permission(Permissions.setting_tenant_role_update)
	@Before(Tx.class)
	public void update() {
		TenantRole role = getModel(TenantRole.class, "", true);
		role.setOperCodes(getParaValues("operCodes"));
		Ret ret = roleService.update(role);

		renderJson(ret);
	}



	/**
	* 删除
	*/
	@Permission(Permissions.setting_tenant_role_delete)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = roleService.delete(Arrays.asList(id));

		renderJson(ret);
	}
	
	/**
	 * 加载公共数据
	 */
	private void setAttrCommon() {
		List<SystemOper> topDataList = SystemOper.dao.findDataTopList();
		List<SystemOper> topFeatureList = SystemOper.dao.findFeatureTopList();
		setAttr("topDataList", topDataList);
		setAttr("topFeatureList", topFeatureList);
	}

}