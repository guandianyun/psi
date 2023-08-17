package com.bytechainx.psi.web.web.controller.fund;


import java.util.ArrayList;
import java.util.List;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.TraderBalanceAccount;
import com.bytechainx.psi.common.model.TraderTransferOrder;
import com.bytechainx.psi.fund.service.AccountInfoService;
import com.bytechainx.psi.fund.service.AccountTransferService;
import com.bytechainx.psi.web.epc.TraderEventProducer;
import com.bytechainx.psi.web.epc.event.fund.AccountTransferEvent;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 账户互转
*/
@Path("/fund/account/transfer")
public class AccountTransferController extends BaseController {

	@Inject
	private AccountTransferService transferService;
	@Inject
	private AccountInfoService accountInfoService;
	@Inject
	private TraderEventProducer traderEventProducer;

	/**
	* 首页
	*/
	@Permission(Permissions.fund_account_transfer)
	public void index() {
		setAttrCommon();
	}

	/**
	* 列表
	*/
	@Permission(Permissions.fund_account_transfer)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Integer outAccountId = getInt("out_account_id");
		Integer inAccountId = getInt("in_account_id");
		Boolean hideDisableFlag = getBoolean("hide_disable_flag"); // 隐藏作废单据
		String keyword = get("keyword");
		Kv condKv = Kv.create();
		condKv.set("out_account_id", outAccountId);
		condKv.set("in_account_id", inAccountId);
		condKv.set("order_code,remark", keyword); // 多字段模糊查询
		if(hideDisableFlag) {
			condKv.set("order_status", OrderStatusEnum.normal.getValue());
		}
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<TraderTransferOrder> page = transferService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
		setAttr("hideDisableFlag", hideDisableFlag);
	}

	/**
	* 查看
	*/
	@Permission(Permissions.fund_account_transfer_show)
	public void show() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TraderTransferOrder transferOrder = TraderTransferOrder.dao.findById(id);
		if(transferOrder == null) {
			renderError(404);
			return;
		}
		setAttr("transferOrder", transferOrder);
	}


	/**
	* 添加
	*/
	@Permission(Permissions.fund_account_transfer_create)
	public void add() {
		setAttrCommon();
	}

	/**
	* 新增
	*/
	@Permission(Permissions.fund_account_transfer_create)
	public void create() {
		TraderTransferOrder transferOrder = getModel(TraderTransferOrder.class, "", true);
		transferOrder.setMakeManId(getAdminId());
		transferOrder.setLastManId(getAdminId());
		
		Ret ret = traderEventProducer.request(getAdminId(), new AccountTransferEvent("create"), transferOrder);
		renderJson(ret);
	}



	/**
	* 编辑
	*/
	@Permission(Permissions.fund_account_transfer_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TraderTransferOrder transferOrder = TraderTransferOrder.dao.findById(id);
		if(transferOrder == null) {
			renderError(404);
			return;
		}
		setAttr("transferOrder", transferOrder);
		setAttrCommon();
		
	}

	/**
	* 修改
	*/
	@Permission(Permissions.fund_account_transfer_update)
	public void update() {
		TraderTransferOrder transferOrder = getModel(TraderTransferOrder.class, "", true);
		transferOrder.setMakeManId(getAdminId());
		transferOrder.setLastManId(getAdminId());
		
		Ret ret = traderEventProducer.request(getAdminId(), new AccountTransferEvent("update"), transferOrder);
		renderJson(ret);
	}


	/**
	* 停用
	*/
	@Permission(Permissions.fund_account_transfer_disable)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		List<Integer> ids = new ArrayList<>();
		ids.add(id);
		
		Ret ret = traderEventProducer.request(getAdminId(), new AccountTransferEvent("disable"), ids);
		renderJson(ret);
	}
	
	
	/**
	 * 设置公共数据
	 */
	private void setAttrCommon() {
		Kv accountCondKv = Kv.create();
		accountCondKv.set("data_status", DataStatusEnum.enable.getValue());
		Page<TraderBalanceAccount> balanceAccountPage = accountInfoService.paginate(accountCondKv, 1, maxPageSize);
		
		setAttr("balanceAccountPage", balanceAccountPage);
	}

}