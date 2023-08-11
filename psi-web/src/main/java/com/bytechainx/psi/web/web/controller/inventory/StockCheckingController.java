package com.bytechainx.psi.web.web.controller.inventory;


import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.api.TraderCenterApi;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.dto.UserSession;
import com.bytechainx.psi.common.model.InventoryChecking;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantPrintTemplate;
import com.bytechainx.psi.common.service.goods.GoodsInfoService;
import com.bytechainx.psi.common.service.setting.TenantAdminService;
import com.bytechainx.psi.purchase.service.StockCheckingService;
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
* 盘点
*/
@Path("/inventory/stock/checking")
public class StockCheckingController extends BaseController {

	@Inject
	private StockCheckingService checkingService;
	@Inject
	private StockWarehouseService warehouseService;
	@Inject
	private TenantAdminService adminService;
	@Inject
	private GoodsInfoService goodsInfoService;

	/**
	* 首页
	*/
	@Permission(Permissions.inventory_stock_checking)
	public void index() {
		
	}

	/**
	* 列表
	*/
	@Permission(Permissions.inventory_stock_checking)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer handlerId = getInt("handler_id");
		Boolean hideDisableFlag = getBoolean("hide_disable_flag"); // 隐藏作废单据
		Boolean hideProfitLossFlag = getBoolean("hide_profit_loss_flag", false); // 隐藏无盈亏单据
		String keyword = get("keyword");
		Kv condKv = Kv.create();
		
		TenantAdmin currentAdmin = getCurrentAdmin();
		UserSession session = getUserSession();
		if(session.hasOper(Permissions.sensitiveData_order_showStockOrder)) { // 有查看他人单据的权限
			if(handlerId != null && handlerId > 0) {
				condKv.set("handler_id", handlerId);
			}
		} else { // 没有查看他人单据权限，只查询自己的
			ConditionFilter filter = new ConditionFilter();
			filter.setOperator(Operator.more);
			filter.setValue(currentAdmin.getId());
			condKv.set("(handler_id = ? or make_man_id = ?)", filter);
		}
		
		condKv.set("order_code,remark", keyword); // 多字段模糊查询
		if(hideDisableFlag) {
			condKv.set("order_status", OrderStatusEnum.normal.getValue());
		} else if(getInt("order_status") != null){
			condKv.set("order_status", getInt("order_status"));
		} else {
			ConditionFilter filter = new ConditionFilter();
			filter.setOperator(Operator.neq);
			filter.setValue(OrderStatusEnum.draft.getValue());
			condKv.set("order_status", filter);
		}
		if(hideProfitLossFlag) {
			ConditionFilter filter = new ConditionFilter();
			filter.setOperator(Operator.neq);
			filter.setValue(0);
			condKv.set("check_number", filter);
		}
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<InventoryChecking> page = checkingService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
		setAttr("hideDisableFlag", hideDisableFlag);
		setAttr("hideProfitLossFlag", hideProfitLossFlag);
		setAttrCommon();
	}
	
	/**
	* 草稿单
	*/
	@Permission(Permissions.inventory_stock_checking)
	public void draftIndex() {
		draftList();
	}
	/**
	* 草稿单列表
	*/
	@Permission(Permissions.inventory_stock_checking)
	public void draftList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Kv condKv = Kv.create();
		condKv.set("make_man_id", getAdminId()); // 只显示当前用户的草稿单
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<InventoryChecking> page = checkingService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
	}
	/**
	* 草稿单列表,JSON格式
	*/
	@Permission(Permissions.inventory_stock_checking)
	public void draftListByJson() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Kv condKv = Kv.create();
		condKv.set("make_man_id", getAdminId()); // 只显示当前用户的草稿单
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<InventoryChecking> page = checkingService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		renderJson(Ret.ok().set("data", page));
	}
	

	/**
	* 查看
	*/
	@Permission(Permissions.inventory_stock_checking_show)
	public void show() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		InventoryChecking stockChecking = InventoryChecking.dao.findById(id);
		if(stockChecking == null) {
			renderError(404);
			return;
		}
		setAttr("stockChecking", stockChecking);
	}


	/**
	* 添加
	*/
	@Permission(Permissions.inventory_stock_checking_create)
	public void add() {
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<InventoryChecking> page = checkingService.paginate(null, null, condKv, 1, 1);
		
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttrCommon();
	}

	/**
	* 新增
	*/
	@Permission(Permissions.inventory_stock_checking_create)
	@Before(Tx.class)
	public void create() {
		String responseJson = TraderCenterApi.requestApi("/inventory/stock/checking/create", getAdminId(), getParaMap());
		renderJson(responseJson);
	}



	/**
	* 编辑
	*/
	@Permission(Permissions.inventory_stock_checking_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		InventoryChecking stockChecking = InventoryChecking.dao.findById(id);
		if(stockChecking == null) {
			renderError(404);
			return;
		}
		setAttr("stockChecking", stockChecking);
		setAttrCommon();
		
	}
	
	/**
	* 编辑草稿单
	*/
	@Permission(Permissions.inventory_stock_checking_update)
	public void editDraft() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		InventoryChecking stockChecking = InventoryChecking.dao.findById(id);
		if(stockChecking == null) {
			renderError(404);
			return;
		}
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<InventoryChecking> page = checkingService.paginate(null, null, condKv, 1, 1);
		
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttr("stockChecking", stockChecking);
		setAttrCommon();
		
	}
	
	/**
	* 修改
	*/
	@Permission(Permissions.inventory_stock_checking_update)
	public void update() {
		String responseJson = TraderCenterApi.requestApi("/inventory/stock/checking/update", getAdminId(), getParaMap());
		renderJson(responseJson);
	}


	/**
	* 停用
	*/
	@Permission(Permissions.inventory_stock_checking_disable)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		String responseJson = TraderCenterApi.requestApi("/inventory/stock/checking/disable", getAdminId(), getParaMap());
		renderJson(responseJson);
	}
	
	/**
	* 审核
	*/
	@Permission(Permissions.inventory_stock_checking_audit)
	public void audit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		setAttr("id", id);
	}
	
	/**
	* 审核
	*/
	@Permission(Permissions.inventory_stock_checking_audit)
	public void auditCreate() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		String responseJson = TraderCenterApi.requestApi("/inventory/stock/checking/audit", getAdminId(), getParaMap());
		renderJson(responseJson);
	}
	
	/**
	* 删除
	*/
	@Permission(Permissions.inventory_stock_checking_update)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = checkingService.delete(id);

		renderJson(ret);
	}
	
	/**
	 * 公共数据
	 */
	private void setAttrCommon() {
		JSONObject printConfirmConfig = TenantConfig.dao.findJsonByKeyCahce(TenantConfigEnum.create_order_print_confirm);
		setAttr("orderPrintConfig", printConfirmConfig);
		setAttr("orderAuditFlag", InventoryChecking.dao.findAuditConfig()); // 是否审核
		setAttr("orderRows", InventoryChecking.dao.findRowsConfig()); // 默认表格行数，后期要做成配置
	}

	/**
	* 显示打印模板
	*/
	@Permission(Permissions.inventory_stock_checking)
	@Before(Tx.class)
	public void showPrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		InventoryChecking stockChecking = InventoryChecking.dao.findById(id);
		if(stockChecking == null) {
			renderError(404);
			return;
		}
		List<TenantPrintTemplate> templateList = stockChecking.getPrintTemplateList();
		setAttr("templateList", templateList);
		setAttr("stockChecking", stockChecking);
	}
	
	/**
	 * 打印或预览
	 */
	@Permission(Permissions.inventory_stock_checking)
	@Before(Tx.class)
	public void print() {
		Integer id = getInt("id");
		Integer templateId = getInt("template_id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		InventoryChecking stockChecking = InventoryChecking.dao.findById(id);
		if(stockChecking == null) {
			renderError(404);
			return;
		}
		TenantPrintTemplate printTemplate;
		if(templateId == null || templateId <= 0) {
			printTemplate = stockChecking.getPrintDefaultTemplate();
		} else {
			printTemplate = TenantPrintTemplate.dao.findById(templateId);
		}
		if(printTemplate == null) {
			renderJson(Ret.fail("打印模板不存在，请先设置打印模板"));
			return;
		}
		Ret ret = Ret.ok();
		ret.set("tpl_content", printTemplate.getContent());
		ret.set("json_data", stockChecking.getPrintData());
		ret.set("order", stockChecking);
		
		renderJson(ret);
	}
	
	/**
	 * 更新打印次数
	 */
	@Permission(Permissions.inventory_stock_checking)
	@Before(Tx.class)
	public void updatePrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		InventoryChecking stockChecking = InventoryChecking.dao.findById(id);
		if(stockChecking == null) {
			renderError(404);
			return;
		}
		stockChecking.setPrintCount(stockChecking.getPrintCount()+1);
		stockChecking.setUpdatedAt(new Date());
		stockChecking.update();
		renderJson(Ret.ok());
	}
	
	
}