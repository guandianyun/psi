package com.bytechainx.psi.web.web.controller.fund;


import java.util.Arrays;

import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.TraderFundType;
import com.bytechainx.psi.fund.service.AccountFundTypeService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 收支项目
*/
@Path("/fund/account/fundType")
public class AccountFundTypeController extends BaseController {

	@Inject
	private AccountFundTypeService accountFundTypeService;

	/**
	* 首页
	*/
	@Permission(Permissions.fund_account_fundType)
	public void index() {
		setAttr("hideStopFlag", true);
	}

	/**
	* 列表
	*/
	@Permission(Permissions.fund_account_fundType)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Boolean hideStopFlag = getBoolean("hide_stop_flag", true); // 隐藏停用
		Kv condKv = Kv.create();
		if(hideStopFlag) {
			condKv.set("data_status", DataStatusEnum.enable.getValue());
		}
		Page<TraderFundType> page = accountFundTypeService.paginate(condKv, pageNumber, pageSize);
		setAttr("page", page);
	}
	
	@Permission(Permissions.fund_account_fundType)
	public void optionList() {
		Integer fundFlow = getInt("fund_flow");
		Kv condKv = Kv.create();
		condKv.set("fund_flow", fundFlow);
		Page<TraderFundType> page = accountFundTypeService.paginate(condKv, 1, maxPageSize);
		setAttr("page", page);
	}
	
	@Permission(Permissions.fund_account_fundType)
	public void listByJson() {
		Integer fundFlow = getInt("fund_flow");
		Kv condKv = Kv.create();
		condKv.set("fund_flow", fundFlow);
		Page<TraderFundType> page = accountFundTypeService.paginate(condKv, 1, maxPageSize);
		renderJson(Ret.ok().set("data", page.getList()));
	}

	/**
	* 查看
	*/
	@Permission(Permissions.fund_account_fundType_show)
	public void show() {

		renderJson(Ret.ok());
	}


	/**
	* 添加
	*/
	@Permission(Permissions.fund_account_fundType_create)
	public void add() {
		setAttr("sourcePage", get("sourcePage"));
	}

	/**
	* 新增
	*/
	@Permission(Permissions.fund_account_fundType_create)
	@Before(Tx.class)
	public void create() {
		TraderFundType fundType = getModel(TraderFundType.class, "", true);
		Ret ret = accountFundTypeService.create(fundType);
		renderJson(ret);
	}



	/**
	* 编辑
	*/
	@Permission(Permissions.fund_account_fundType_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TraderFundType fundType = TraderFundType.dao.findById(id);
		if(fundType == null) {
			renderError(404);
			return;
		}
		setAttr("accountFundType", fundType);
	}

	/**
	* 修改
	*/
	@Permission(Permissions.fund_account_fundType_update)
	@Before(Tx.class)
	public void update() {
		TraderFundType fundType = getModel(TraderFundType.class, "", true);
		Ret ret = accountFundTypeService.update(fundType);
		renderJson(ret);
	}



	/**
	* 删除
	*/
	@Permission(Permissions.fund_account_fundType_delete)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = accountFundTypeService.delete(Arrays.asList(id));
		renderJson(ret);
	}
	
	/**
	* 停用
	*/
	@Permission(Permissions.fund_account_fundType_disable)
	@Before(Tx.class)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = accountFundTypeService.disable(Arrays.asList(id));

		renderJson(ret);
	}
	
	/**
	* 启用
	*/
	@Permission(Permissions.fund_account_fundType_disable)
	@Before(Tx.class)
	public void enable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = accountFundTypeService.enable(Arrays.asList(id));

		renderJson(ret);
	}

}