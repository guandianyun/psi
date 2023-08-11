package com.bytechainx.psi.web.web.controller.sale;


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
import com.bytechainx.psi.common.model.SaleOrder;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantPrintTemplate;
import com.bytechainx.psi.sale.service.SaleOrderService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 销售单
*/
@Path("/sale/sale/order")
public class SaleOrderController extends BaseController {


	@Inject
	private SaleOrderService orderService;

	/**
	* 首页
	*/
	@Permission(Permissions.sale_sale_order)
	public void index() {
		
	}

	/**
	* 列表
	*/
	@Permission(Permissions.sale_sale_order)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Kv condKv = conditions();
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<SaleOrder> page = orderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
		setAttr("hideDisableFlag", getBoolean("hide_disable_flag"));
		setAttr("showAuditFlag", getBoolean("show_audit_flag"));
		setAttr("hidePayFlag", getBoolean("hide_pay_flag"));
		setAttrCommon();
	}

	
	/**
	* 草稿单
	*/
	@Permission(Permissions.sale_sale_order)
	public void draftIndex() {
		draftList();
	}
	/**
	* 草稿单列表
	*/
	@Permission(Permissions.sale_sale_order)
	public void draftList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer customerInfoId = getInt("customer_info_id");
		Kv condKv = Kv.create();
		condKv.set("customer_info_id", customerInfoId);
		condKv.set("make_man_id", getAdminId()); // 只显示当前用户的草稿单
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<SaleOrder> page = orderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
	}
	/**
	* 草稿单列表,JSON格式
	*/
	@Permission(Permissions.sale_sale_order)
	public void draftListByJson() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer customerInfoId = getInt("customer_info_id");
		Integer tenantStoreId = getInt("tenant_store_id");
		Kv condKv = Kv.create();
		condKv.set("customer_info_id", customerInfoId);
		condKv.set("tenant_store_id", tenantStoreId);
		condKv.set("make_man_id", getAdminId()); // 只显示当前用户的草稿单
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<SaleOrder> page = orderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		renderJson(Ret.ok().set("data", page));
	}
	

	/**
	* 查看
	*/
	@Permission(Permissions.sale_sale_order_show)
	public void show() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		SaleOrder saleOrder = SaleOrder.dao.findById(id);
		if(saleOrder == null) {
			renderError(404);
			return;
		}
		setAttrCommon();
		setAttr("saleOrder", saleOrder);
	}
	
	/**
	* 收入款日志
	*/
	@Permission(Permissions.sale_sale_order_show)
	public void showPayLog() {
		show();
	}
	
	/**
	* 单据操作日志
	*/
	@Permission(Permissions.sale_sale_order_show)
	public void showOrderLog() {
		show();
	}


	/**
	* 添加
	*/
	@Permission(Permissions.sale_sale_order_create)
	public void add() {
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<SaleOrder> page = orderService.paginate(null, null, condKv, 1, 1);
		
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttrCommon();
		
	}

	/**
	* 新增
	*/
	@Permission(Permissions.sale_sale_order_create)
	public void create() {
		String responseJson = TraderCenterApi.requestApi("/sale/sale/order/create", getAdminId(), getParaMap());
		renderJson(responseJson);
	}

	/**
	* 编辑
	*/
	@Permission(Permissions.sale_sale_order_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		SaleOrder saleOrder = SaleOrder.dao.findById(id);
		if(saleOrder == null) {
			renderError(404);
			return;
		}
		setAttr("saleOrder", saleOrder);
		setAttrCommon();
		
	}
	
	/**
	* 编辑草稿单
	*/
	@Permission(Permissions.sale_sale_order_update)
	public void editDraft() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		SaleOrder saleOrder = SaleOrder.dao.findById(id);
		if(saleOrder == null) {
			renderError(404);
			return;
		}
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<SaleOrder> page = orderService.paginate(null, null, condKv, 1, 1);
		
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttr("saleOrder", saleOrder);
		setAttrCommon();
		
	}
	
	/**
	* 修改
	*/
	@Permission(Permissions.sale_sale_order_update)
	public void update() {
		String responseJson = TraderCenterApi.requestApi("/sale/sale/order/update", getAdminId(), getParaMap());
		renderJson(responseJson);
	}


	/**
	* 停用
	*/
	@Permission(Permissions.sale_sale_order_disable)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		String responseJson = TraderCenterApi.requestApi("/sale/sale/order/disable", getAdminId(), getParaMap());
		renderJson(responseJson);
	}
	
	/**
	* 审核
	*/
	@Permission(Permissions.sale_sale_order_audit)
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
	@Permission(Permissions.sale_sale_order_audit)
	public void auditCreate() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		String responseJson = TraderCenterApi.requestApi("/sale/sale/order/audit", getAdminId(), getParaMap());
		renderJson(responseJson);
	}
	
	/**
	* 删除
	*/
	@Permission(Permissions.sale_sale_order_update)
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
		TenantConfig orderGoodsDiscountConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_goods_discount);
		setAttr("orderGoodsDiscountFlag", Boolean.parseBoolean(orderGoodsDiscountConfig.getAttrValue()));
		
		TenantConfig lowestPriceConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.sale_create_price_lowest);
		setAttr("lowestPriceFlag", Boolean.parseBoolean(lowestPriceConfig.getAttrValue()));
		
		TenantConfig receiptFundAllConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.sale_receipt_fund_all);
		setAttr("receiptFundAllFlag", Boolean.parseBoolean(receiptFundAllConfig.getAttrValue()));
		
		TenantConfig showGoodsProfitConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.sale_show_goods_profit);
		setAttr("showGoodsProfitFlag", Boolean.parseBoolean(showGoodsProfitConfig.getAttrValue()));
		
		TenantConfig saleNegativeStockConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.sale_negative_stock);
		setAttr("saleNegativeStockFlag", Boolean.parseBoolean(saleNegativeStockConfig.getAttrValue()));
		
		JSONObject printConfirmConfig = TenantConfig.dao.findJsonByKeyCahce(TenantConfigEnum.create_order_print_confirm);
		
		setAttr("orderPrintConfig", printConfirmConfig);
		setAttr("orderAuditFlag", SaleOrder.dao.findAuditConfig()); // 是否审核
		setAttr("orderRows", SaleOrder.dao.findRowsConfig()); // 默认表格行数，后期要做成配置
		setAttr("feeList", SaleOrder.dao.findFeeConfig()); // 其他费用配置
		setAttr("costList", SaleOrder.dao.findCostConfig()); // 成本支出配置
		
		TenantConfig orderFundPaytimeConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_fund_paytime);
		setAttr("orderFundPaytimeFlag", Boolean.parseBoolean(orderFundPaytimeConfig.getAttrValue()));
	}
	
	
	/**
	* 显示打印模板
	*/
	@Permission(Permissions.sale_sale_order)
	@Before(Tx.class)
	public void showPrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		SaleOrder saleOrder = SaleOrder.dao.findById(id);
		if(saleOrder == null) {
			renderError(404);
			return;
		}
		List<TenantPrintTemplate> templateList = saleOrder.getPrintTemplateList();
		setAttr("templateList", templateList);
		setAttr("saleOrder", saleOrder);
	}
	
	/**
	 * 打印或预览
	 */
	@Permission(Permissions.sale_sale_order)
	@Before(Tx.class)
	public void print() {
		Integer id = getInt("id");
		Integer templateId = getInt("template_id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		SaleOrder saleOrder = SaleOrder.dao.findById(id);
		if(saleOrder == null) {
			renderError(404);
			return;
		}
		TenantPrintTemplate printTemplate;
		if(templateId == null || templateId <= 0) {
			printTemplate = saleOrder.getPrintDefaultTemplate();
		} else {
			printTemplate = TenantPrintTemplate.dao.findById(templateId);
		}
		if(printTemplate == null) {
			renderJson(Ret.fail("打印模板不存在，请先设置打印模板"));
			return;
		}
		Ret ret = Ret.ok();
		ret.set("tpl_content", printTemplate.getContent());
		ret.set("json_data", saleOrder.getPrintData());
		ret.set("order", saleOrder);
		
		renderJson(ret);
	}
	
	/**
	 * 更新打印次数
	 */
	@Permission(Permissions.sale_sale_order)
	@Before(Tx.class)
	public void updatePrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		SaleOrder saleOrder = SaleOrder.dao.findById(id);
		if(saleOrder == null) {
			renderError(404);
			return;
		}
		saleOrder.setPrintCount(saleOrder.getPrintCount()+1);
		saleOrder.setUpdatedAt(new Date());
		saleOrder.update();
		renderJson(Ret.ok());
	}
	
	
	/**
	 * 导出
	 */
	@Permission(Permissions.sale_sale_order_export)
	public void export() {
		Kv condKv = conditions();
		String startTime = get("start_time");
		String endTime = get("end_time");
		Ret ret = orderService.export(getAdminId(), startTime, endTime, condKv);
		renderJson(ret);
	}
	
	/**
	 * 导出单个销售单,再打印模板满足不了客户的情况下，客户导出销售单详情，然后再加工打印
	 */
	@Permission(Permissions.sale_sale_order_export)
	public void exportOrder() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		Ret ret = orderService.exportOrder(getAdminId(), id);
		renderJson(ret);
	}
	
	/**
	 * 查询条件
	 * @return
	 */
	private Kv conditions() {
		Integer customerInfoId = getInt("customer_info_id");
		Integer handlerId = getInt("handler_id");
		Boolean hideDisableFlag = getBoolean("hide_disable_flag"); // 隐藏作废单据
		Boolean hidePayFlag = getBoolean("hide_pay_flag"); // 隐藏已付清单据
		Boolean showAuditFlag = getBoolean("show_audit_flag"); // 只显示待审核单据
		
		String keyword = get("keyword");
		Kv condKv = Kv.create();
		conditionFilterStore(condKv, Permissions.sale_sale); // 添加门店过滤条件
		condKv.set("customer_info_id", customerInfoId);
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