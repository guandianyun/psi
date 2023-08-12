package com.bytechainx.psi.web.web.controller.setting;


import com.bytechainx.psi.common.EnumConstant.FlagEnum;
import com.bytechainx.psi.common.EnumConstant.UserActiveStatusEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TenantOperLog;
import com.bytechainx.psi.common.service.setting.SystemOperLogService;
import com.bytechainx.psi.common.service.setting.TenantAdminService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 操作日志
*/
@Path("/setting/system/operLog")
public class SystemOperLogController extends BaseController {

	@Inject
	private SystemOperLogService operLogService;
	@Inject
	private TenantAdminService adminService;

	/**
	* 首页
	*/
	@Permission(Permissions.setting_system_operLog)
	public void index() {
		Kv condkv = Kv.create();
		condkv.set("active_status", UserActiveStatusEnum.enable.getValue());
		condkv.set("login_flag", FlagEnum.YES.getValue());
		Page<TenantAdmin>  adminPage = adminService.paginate(condkv, 1, maxPageSize);
		setAttr("adminPage", adminPage);
	}

	/**
	* 列表
	*/
	@Permission(Permissions.setting_system_operLog)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Kv condkv = Kv.create();
		condkv.set("tenant_admin_id", getInt("tenant_admin_id"));
		condkv.set("log_type", getInt("log_type"));
		condkv.set("oper_desc", get("keyword"));
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<TenantOperLog> page = operLogService.paginate(condkv, startTime, endTime, pageNumber, pageSize);
		setAttr("page", page);
	}

	/**
	* 查看
	*/
	@Permission(Permissions.setting_system_operLog_show)
	public void show() {

		renderJson(Ret.ok());
	}
}