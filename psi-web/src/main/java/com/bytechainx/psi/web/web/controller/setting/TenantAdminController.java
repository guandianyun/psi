package com.bytechainx.psi.web.web.controller.setting;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bytechainx.psi.common.EnumConstant.UserActiveStatusEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.service.setting.TenantAdminService;
import com.bytechainx.psi.common.service.setting.TenantRoleService;
import com.bytechainx.psi.purchase.service.StockWarehouseService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 员工管理
*/
@Path("/setting/tenant/admin")
public class TenantAdminController extends BaseController {
	
	@Inject
	private TenantAdminService adminService;
	@Inject
	private TenantRoleService roleService;
	@Inject
	private StockWarehouseService warehouseService;

	/**
	* 首页
	*/
	@Permission(Permissions.setting_tenant_admin)
	public void index() {
		setAttr("hideStopFlag", true);
	}

	/**
	* 列表
	*/
	@Permission(Permissions.setting_tenant_admin)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Boolean hideStopFlag = getBoolean("hide_stop_flag", true); // 隐藏停用
		Kv condKv = Kv.create();
		if(hideStopFlag) {
			ConditionFilter filter = new ConditionFilter();
			filter.setOperator(Operator.neq);
			filter.setValue(UserActiveStatusEnum.disable.getValue());
			condKv.set("active_status", filter);
		}
		Page<TenantAdmin> page = adminService.paginate(condKv, pageNumber, pageSize);
		setAttr("page", page);
	}
	
	/**
	* 查看
	*/
	@Permission(Permissions.setting_tenant_admin_show)
	public void show() {

		renderJson(Ret.ok());
	}


	/**
	* 添加
	*/
	@Permission(Permissions.setting_tenant_admin_create)
	public void add() {
		setAttrCommon();
	}

	/**
	* 修改
	*/
	@Permission(Permissions.setting_tenant_admin_create)
	@Before(Tx.class)
	public void create() {
		TenantAdmin admin = getModel(TenantAdmin.class, "", true);
		Ret ret = adminService.create(admin);
		
		renderJson(ret);
	}

	/**
	* 编辑
	*/
	@Permission(Permissions.setting_tenant_admin_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TenantAdmin admin = TenantAdmin.dao.findById(id);
		if(admin == null) {
			renderError(404);
			return;
		}
		setAttrCommon();
		admin.remove("password", "encrypt");
		setAttr("tenantAdmin", admin);
	}

	/**
	* 修改
	*/
	@Permission(Permissions.setting_tenant_admin_update)
	@Before(Tx.class)
	public void update() {
		TenantAdmin admin = getModel(TenantAdmin.class, "", true);
		Ret ret = adminService.update(admin);

		renderJson(ret);
	}



	/**
	* 停用
	*/
	@Permission(Permissions.setting_tenant_admin_disable)
	@Before(Tx.class)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = adminService.disable(Arrays.asList(id));

		renderJson(ret);
	}
	
	/**
	* 启用
	*/
	@Permission(Permissions.setting_tenant_admin_disable)
	@Before(Tx.class)
	public void enable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = adminService.enable(Arrays.asList(id));

		renderJson(ret);
	}

	/**
	 * 加载公共数据
	 */
	private void setAttrCommon() {
		Page<?> rolePage = roleService.paginate(1, maxPageSize);
		setAttr("roleList", rolePage.getList());
	}
	
	/**
	 * 经手人列表
	 */
	@Permission({Permissions.inventory_purchase, Permissions.inventory_stock, Permissions.fund, Permissions.sale})
	public void listByJson() {
		int pageNumber = getInt("pageNumber", 1);
		String keyword = get("keyword");
		Kv condKv = Kv.create();
		condKv.set("active_status", UserActiveStatusEnum.enable.getValue());
		condKv.set("real_name,code", keyword); // 多字段模糊查询
		TenantAdmin currentAdmin = getCurrentAdmin();
		
		List<TenantAdmin> adminList = new ArrayList<>();
		Page<TenantAdmin> page = adminService.paginate(condKv, pageNumber, maxPageSize);
		for (TenantAdmin admin : page.getList()) {
			// 当前用户是主帐号，或者遍历员工是主帐号，或者遍历员工有所有门店权限
			if(currentAdmin.getMainAccountFlag() || admin.getMainAccountFlag()) {
				adminList.add(admin);
				continue;
			}
		}
		page.setList(adminList);
		renderJson(Ret.ok().set("data", page.getList()));
	}
	
}