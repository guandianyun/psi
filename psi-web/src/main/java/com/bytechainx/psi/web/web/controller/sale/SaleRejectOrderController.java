package com.bytechainx.psi.web.web.controller.sale;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderPayStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.RejectDealTypeEnum;
import com.bytechainx.psi.common.EnumConstant.RejectReasonTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.api.TraderCenterApi;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.model.SaleRejectOrder;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantPrintTemplate;
import com.bytechainx.psi.sale.service.SaleRejectOrderService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 销售退货单
*/
@Path("/sale/sale/rejectOrder")
public class SaleRejectOrderController extends BaseController {

	@Inject
	private SaleRejectOrderService rejectOrderService;

	/**
	* 首页
	*/
	@Permission(Permissions.sale_sale_rejectOrder)
	public void index() {
		
	}

	/**
	* 列表
	*/
	@Permission(Permissions.sale_sale_rejectOrder)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Kv condKv = conditions();
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<SaleRejectOrder> page = rejectOrderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
		setAttr("hideDisableFlag", getBoolean("hide_disable_flag"));
		setAttr("showAuditFlag", getBoolean("show_audit_flag"));
		setAttr("hidePayFlag", getBoolean("hide_pay_flag"));
		setAttrCommon();
	}

	/**
	* 草稿单
	*/
	@Permission(Permissions.sale_sale_rejectOrder)
	public void draftIndex() {
		draftList();
	}
	/**
	* 草稿单列表
	*/
	@Permission(Permissions.sale_sale_rejectOrder)
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
		
		Page<SaleRejectOrder> page = rejectOrderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
	}
	/**
	* 草稿单列表,JSON格式
	*/
	@Permission(Permissions.sale_sale_rejectOrder)
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
		
		Page<SaleRejectOrder> page = rejectOrderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		renderJson(Ret.ok().set("data", page));
	}
	

	/**
	* 查看
	*/
	@Permission(Permissions.sale_sale_rejectOrder_show)
	public void show() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		SaleRejectOrder saleRejectOrder = SaleRejectOrder.dao.findById(id);
		if(saleRejectOrder == null) {
			renderError(404);
			return;
		}
		setAttrCommon();
		setAttr("saleRejectOrder", saleRejectOrder);
	}
	
	/**
	* 收入款日志
	*/
	@Permission(Permissions.sale_sale_rejectOrder_show)
	public void showPayLog() {
		show();
	}
	
	/**
	* 单据操作日志
	*/
	@Permission(Permissions.sale_sale_rejectOrder_show)
	public void showOrderLog() {
		show();
	}

	/**
	* 添加
	*/
	@Permission(Permissions.sale_sale_rejectOrder_create)
	public void add() {
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<SaleRejectOrder> page = rejectOrderService.paginate(null, null, condKv, 1, 1);
		
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttrCommon();
		
	}

	/**
	* 新增
	*/
	@Permission(Permissions.sale_sale_rejectOrder_create)
	public void create() {
		String responseJson = TraderCenterApi.requestApi("/sale/sale/rejectOrder/create", getAdminId(), getParaMap());
		renderJson(responseJson);
	}

	/**
	* 编辑
	*/
	@Permission(Permissions.sale_sale_rejectOrder_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		SaleRejectOrder saleRejectOrder = SaleRejectOrder.dao.findById(id);
		if(saleRejectOrder == null) {
			renderError(404);
			return;
		}
		setAttr("saleRejectOrder", saleRejectOrder);
		setAttrCommon();
		
	}
	
	/**
	* 编辑草稿单
	*/
	@Permission(Permissions.sale_sale_rejectOrder_update)
	public void editDraft() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		SaleRejectOrder saleRejectOrder = SaleRejectOrder.dao.findById(id);
		if(saleRejectOrder == null) {
			renderError(404);
			return;
		}
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<SaleRejectOrder> page = rejectOrderService.paginate(null, null, condKv, 1, 1);
		
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttr("saleRejectOrder", saleRejectOrder);
		setAttrCommon();
		
	}
	
	/**
	* 修改
	*/
	@Permission(Permissions.sale_sale_rejectOrder_update)
	public void update() {
		String responseJson = TraderCenterApi.requestApi("/sale/sale/rejectOrder/update", getAdminId(), getParaMap());
		renderJson(responseJson);
	}


	/**
	* 停用
	*/
	@Permission(Permissions.sale_sale_rejectOrder_disable)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		String responseJson = TraderCenterApi.requestApi("/sale/sale/rejectOrder/disable", getAdminId(), getParaMap());
		renderJson(responseJson);
	}
	
	/**
	* 审核
	*/
	@Permission(Permissions.sale_sale_rejectOrder_audit)
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
	@Permission(Permissions.sale_sale_rejectOrder_audit)
	public void auditCreate() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		String responseJson = TraderCenterApi.requestApi("/sale/sale/rejectOrder/audit", getAdminId(), getParaMap());
		renderJson(responseJson);
	}
	
	/**
	* 删除
	*/
	@Permission(Permissions.sale_sale_rejectOrder_update)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = rejectOrderService.delete(id);

		renderJson(ret);
	}
	
	/**
	 * 公共数据
	 */
	private void setAttrCommon() {
		TenantConfig orderGoodsDiscountConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_goods_discount);
		setAttr("orderGoodsDiscountFlag", Boolean.parseBoolean(orderGoodsDiscountConfig.getAttrValue()));
		
		TenantConfig lowestPriceConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.sale_create_price_lowest);
		JSONObject printConfirmConfig = TenantConfig.dao.findJsonByKeyCahce(TenantConfigEnum.create_order_print_confirm);
		
		setAttr("orderPrintConfig", printConfirmConfig);
		setAttr("lowestPriceFlag", Boolean.parseBoolean(lowestPriceConfig.getAttrValue()));
		setAttr("orderAuditFlag", SaleRejectOrder.dao.findAuditConfig()); // 是否审核
		setAttr("orderRows", SaleRejectOrder.dao.findRowsConfig()); // 默认表格行数，后期要做成配置
		setAttr("feeList", SaleRejectOrder.dao.findFeeConfig()); // 其他费用配置
		setAttr("costList", SaleRejectOrder.dao.findCostConfig()); // 成本支出配置
		
		TenantConfig orderFundPaytimeConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_fund_paytime);
		setAttr("orderFundPaytimeFlag", Boolean.parseBoolean(orderFundPaytimeConfig.getAttrValue()));
	}
	
	
	/**
	 * 退货原因
	 */
	@Permission(Permissions.sale_sale_rejectOrder)
	public void rejectReasonTypeList() {
		List<Kv> kvList = new ArrayList<>();
		for(RejectReasonTypeEnum e : RejectReasonTypeEnum.values()) {
			Kv kv = Kv.create();
			kv.set("id", e.getValue());
			kv.set("name", e.getName());
			kvList.add(kv);
		}
		renderJson(Ret.ok().set("data", kvList));
	}
	
	/**
	 * 退货如何处理
	 */
	@Permission(Permissions.sale_sale_rejectOrder)
	public void rejectDealTypeList() {
		List<Kv> kvList = new ArrayList<>();
		for(RejectDealTypeEnum e : RejectDealTypeEnum.values()) {
			Kv kv = Kv.create();
			kv.set("id", e.getValue());
			kv.set("name", e.getName());
			kvList.add(kv);
		}
		renderJson(Ret.ok().set("data", kvList));
	}
	

	/**
	* 显示打印模板
	*/
	@Permission(Permissions.sale_sale_rejectOrder)
	@Before(Tx.class)
	public void showPrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		SaleRejectOrder saleRejectOrder = SaleRejectOrder.dao.findById(id);
		if(saleRejectOrder == null) {
			renderError(404);
			return;
		}
		List<TenantPrintTemplate> templateList = saleRejectOrder.getPrintTemplateList();
		setAttr("templateList", templateList);
		setAttr("saleRejectOrder", saleRejectOrder);
	}
	
	/**
	 * 打印或预览
	 */
	@Permission(Permissions.sale_sale_rejectOrder)
	@Before(Tx.class)
	public void print() {
		Integer id = getInt("id");
		Integer templateId = getInt("template_id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		SaleRejectOrder saleRejectOrder = SaleRejectOrder.dao.findById(id);
		if(saleRejectOrder == null) {
			renderError(404);
			return;
		}
		TenantPrintTemplate printTemplate;
		if(templateId == null || templateId <= 0) {
			printTemplate = saleRejectOrder.getPrintDefaultTemplate();
		} else {
			printTemplate = TenantPrintTemplate.dao.findById(templateId);
		}
		if(printTemplate == null) {
			renderJson(Ret.fail("打印模板不存在，请先设置打印模板"));
			return;
		}
		Ret ret = Ret.ok();
		ret.set("tpl_content", printTemplate.getContent());
		ret.set("json_data", saleRejectOrder.getPrintData());
		ret.set("order", saleRejectOrder);
		
		renderJson(ret);
	}
	
	/**
	 * 更新打印次数
	 */
	@Permission(Permissions.sale_sale_rejectOrder)
	@Before(Tx.class)
	public void updatePrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		SaleRejectOrder saleRejectOrder = SaleRejectOrder.dao.findById(id);
		if(saleRejectOrder == null) {
			renderError(404);
			return;
		}
		saleRejectOrder.setPrintCount(saleRejectOrder.getPrintCount()+1);
		saleRejectOrder.setUpdatedAt(new Date());
		saleRejectOrder.update();
		renderJson(Ret.ok());
	}
	
	
	/**
	 * 导出
	 */
	@Permission(Permissions.sale_sale_rejectOrder_export)
	public void export() {
		Kv condKv = conditions();
		String startTime = get("start_time");
		String endTime = get("end_time");
		Ret ret = rejectOrderService.export(getAdminId(), startTime, endTime, condKv);
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