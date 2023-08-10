package com.bytechainx.psi.web.web.controller.inventory;


import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderPayStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.api.TraderCenterApi;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.model.PurchaseOrder;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantPrintTemplate;
import com.bytechainx.psi.purchase.service.PurchaseOrderService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 进货单
*/
@Path("/inventory/purchase/order")
public class PurchaseOrderController extends BaseController {

	@Inject
	private PurchaseOrderService orderService;

	/**
	* 首页
	*/
	@Permission(Permissions.inventory_purchase_order)
	public void index() {
		
	}

	/**
	* 列表
	*/
	@Permission(Permissions.inventory_purchase_order)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Kv condKv = conditions();
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<PurchaseOrder> page = orderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
		setAttr("hideDisableFlag", getBoolean("hide_disable_flag"));
		setAttr("showAuditFlag", getBoolean("show_audit_flag"));
		setAttr("hidePayFlag", getBoolean("hide_pay_flag"));
		setAttrCommon();
	}

	
	/**
	* 草稿单
	*/
	@Permission(Permissions.inventory_purchase_order)
	public void draftIndex() {
		draftList();
	}
	/**
	* 草稿单列表
	*/
	@Permission(Permissions.inventory_purchase_order)
	public void draftList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer supplierInfoId = getInt("supplier_info_id");
		Kv condKv = Kv.create();
		condKv.set("supplier_info_id", supplierInfoId);
		condKv.set("make_man_id", getAdminId()); // 只显示当前用户的草稿单
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<PurchaseOrder> page = orderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
	}
	/**
	* 草稿单列表,JSON格式
	*/
	@Permission(Permissions.inventory_purchase_order)
	public void draftListByJson() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer supplierInfoId = getInt("supplier_info_id");
		Integer tenantStoreId = getInt("tenant_store_id");
		Kv condKv = Kv.create();
		condKv.set("supplier_info_id", supplierInfoId);
		condKv.set("tenant_store_id", tenantStoreId);
		condKv.set("make_man_id", getAdminId()); // 只显示当前用户的草稿单
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<PurchaseOrder> page = orderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		renderJson(Ret.ok().set("data", page));
	}
	

	/**
	* 查看
	*/
	@Permission(Permissions.inventory_purchase_order_show)
	public void show() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		PurchaseOrder purchaseOrder = PurchaseOrder.dao.findById(id);
		if(purchaseOrder == null) {
			renderError(404);
			return;
		}
		setAttrCommon();
		setAttr("purchaseOrder", purchaseOrder);
	}
	
	/**
	* 收入款日志
	*/
	@Permission(Permissions.inventory_purchase_order_show)
	public void showPayLog() {
		show();
	}
	
	/**
	* 单据操作日志
	*/
	@Permission(Permissions.inventory_purchase_order_show)
	public void showOrderLog() {
		show();
	}


	/**
	* 添加
	*/
	@Permission(Permissions.inventory_purchase_order_create)
	public void add() {
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<PurchaseOrder> page = orderService.paginate(null, null, condKv, 1, 1);
		
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttrCommon();
		
	}

	/**
	* 新增
	*/
	@Permission(Permissions.inventory_purchase_order_create)
	public void create() {
		String responseJson = TraderCenterApi.requestApi("/inventory/purchase/order/create", getAdminId(), getParaMap());
		renderJson(responseJson);
	}

	/**
	* 编辑
	*/
	@Permission(Permissions.inventory_purchase_order_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		PurchaseOrder purchaseOrder = PurchaseOrder.dao.findById(id);
		if(purchaseOrder == null) {
			renderError(404);
			return;
		}
		setAttr("purchaseOrder", purchaseOrder);
		setAttrCommon();
		
	}
	
	/**
	* 编辑草稿单
	*/
	@Permission(Permissions.inventory_purchase_order_update)
	public void editDraft() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		PurchaseOrder purchaseOrder = PurchaseOrder.dao.findById(id);
		if(purchaseOrder == null) {
			renderError(404);
			return;
		}
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<PurchaseOrder> page = orderService.paginate(null, null, condKv, 1, 1);
		
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttr("purchaseOrder", purchaseOrder);
		setAttrCommon();
		
	}
	
	/**
	* 修改
	*/
	@Permission(Permissions.inventory_purchase_order_update)
	public void update() {
		String responseJson = TraderCenterApi.requestApi("/inventory/purchase/order/update", getAdminId(), getParaMap());
		renderJson(responseJson);
	}


	/**
	* 停用
	*/
	@Permission(Permissions.inventory_purchase_order_disable)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		String responseJson = TraderCenterApi.requestApi("/inventory/purchase/order/disable", getAdminId(), getParaMap());
		renderJson(responseJson);
	}
	
	/**
	* 审核
	*/
	@Permission(Permissions.inventory_purchase_order_audit)
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
	@Permission(Permissions.inventory_purchase_order_audit)
	public void auditCreate() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		String responseJson = TraderCenterApi.requestApi("/inventory/purchase/order/audit", getAdminId(), getParaMap());
		renderJson(responseJson);
	}
	
	/**
	* 删除
	*/
	@Permission(Permissions.inventory_purchase_order_update)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = orderService.delete(id);

		renderJson(ret);
	}
	
	/**
	 * 公共数据
	 */
	private void setAttrCommon() {
		TenantConfig receiptFundAllConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.purchase_receipt_fund_all);
		setAttr("receiptFundAllFlag", Boolean.parseBoolean(receiptFundAllConfig.getAttrValue()));
		
		TenantConfig orderGoodsDiscountConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_goods_discount);
		setAttr("orderGoodsDiscountFlag", Boolean.parseBoolean(orderGoodsDiscountConfig.getAttrValue()));
		
		TenantConfig purchaseEditSalePriceConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.purchase_edit_sale_price);
		setAttr("purchaseEditSalePriceFlag", Boolean.parseBoolean(purchaseEditSalePriceConfig.getAttrValue()));
		
		JSONObject printConfirmConfig = TenantConfig.dao.findJsonByKeyCahce(TenantConfigEnum.create_order_print_confirm);
		setAttr("orderPrintConfig", printConfirmConfig);
		setAttr("orderAuditFlag", PurchaseOrder.dao.findAuditConfig()); // 是否审核
		setAttr("orderRows", PurchaseOrder.dao.findRowsConfig()); // 默认表格行数，后期要做成配置
		setAttr("feeList", PurchaseOrder.dao.findFeeConfig()); // 其他费用配置
		setAttr("costList", PurchaseOrder.dao.findCostConfig()); // 成本支出配置
		
		TenantConfig orderFundPaytimeConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_fund_paytime);
		setAttr("orderFundPaytimeFlag", Boolean.parseBoolean(orderFundPaytimeConfig.getAttrValue()));
	}
	
	
	/**
	* 显示打印模板
	*/
	@Permission(Permissions.inventory_purchase_order)
	@Before(Tx.class)
	public void showPrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		PurchaseOrder purchaseOrder = PurchaseOrder.dao.findById(id);
		if(purchaseOrder == null) {
			renderError(404);
			return;
		}
		List<TenantPrintTemplate> templateList = purchaseOrder.getPrintTemplateList();
		setAttr("templateList", templateList);
		setAttr("purchaseOrder", purchaseOrder);
	}
	
	/**
	 * 打印或预览
	 */
	@Permission(Permissions.inventory_purchase_order)
	@Before(Tx.class)
	public void print() {
		Integer id = getInt("id");
		Integer templateId = getInt("template_id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		PurchaseOrder purchaseOrder = PurchaseOrder.dao.findById(id);
		if(purchaseOrder == null) {
			renderError(404);
			return;
		}
		TenantPrintTemplate printTemplate;
		if(templateId == null || templateId <= 0) {
			printTemplate = purchaseOrder.getPrintDefaultTemplate();
		} else {
			printTemplate = TenantPrintTemplate.dao.findById(templateId);
		}
		if(printTemplate == null) {
			renderJson(Ret.fail("打印模板不存在，请先设置打印模板"));
			return;
		}
		Ret ret = Ret.ok();
		ret.set("tpl_content", printTemplate.getContent());
		ret.set("json_data", purchaseOrder.getPrintData());
		ret.set("order", purchaseOrder);
		
		renderJson(ret);
	}
	
	/**
	 * 更新打印次数
	 */
	@Permission(Permissions.inventory_purchase_order)
	@Before(Tx.class)
	public void updatePrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		PurchaseOrder purchaseOrder = PurchaseOrder.dao.findById(id);
		if(purchaseOrder == null) {
			renderError(404);
			return;
		}
		purchaseOrder.setPrintCount(purchaseOrder.getPrintCount()+1);
		purchaseOrder.setUpdatedAt(new Date());
		purchaseOrder.update();
		renderJson(Ret.ok());
	}
	
	
	/**
	 * 导出
	 */
	@Permission(Permissions.inventory_purchase_order_export)
	public void export() {
		Kv condKv = conditions();
		String startTime = get("start_time");
		String endTime = get("end_time");
		Ret ret = orderService.export(getAdminId(), startTime, endTime, condKv);
		renderJson(ret);
	}
	
	/**
	 * 查询条件
	 * @return
	 */
	private Kv conditions() {
		Integer supplierInfoId = getInt("supplier_info_id");
		Integer handlerId = getInt("handler_id");
		Boolean hideDisableFlag = getBoolean("hide_disable_flag"); // 隐藏作废单据
		Boolean hidePayFlag = getBoolean("hide_pay_flag"); // 隐藏已付清单据
		Boolean showAuditFlag = getBoolean("show_audit_flag"); // 只显示待审核单据
		
		String keyword = get("keyword");
		Kv condKv = Kv.create();
		conditionFilterStore(condKv, Permissions.inventory_purchase); // 添加门店过滤条件
		condKv.set("supplier_info_id", supplierInfoId);
		condKv.set("handler_id", handlerId);
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
		if(showAuditFlag) {
			condKv.set("audit_status", AuditStatusEnum.waiting.getValue());
		}
		if(hidePayFlag) {
			ConditionFilter filter = new ConditionFilter();
			filter.setOperator(Operator.neq);
			filter.setValue(OrderPayStatusEnum.finish.getValue());
			condKv.set("pay_status", filter);
		}
		return condKv;
	}

}