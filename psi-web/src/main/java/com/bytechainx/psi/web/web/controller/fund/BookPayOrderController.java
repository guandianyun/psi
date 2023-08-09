package com.bytechainx.psi.web.web.controller.fund;


import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderPayStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.RejectTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.api.TraderCenterApi;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.model.PurchaseOrder;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantPrintTemplate;
import com.bytechainx.psi.common.model.TraderPayOrder;
import com.bytechainx.psi.common.model.TraderPayOrderRef;
import com.bytechainx.psi.fund.service.BookPayOrderService;
import com.bytechainx.psi.purchase.service.PurchaseOrderService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.NotAction;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 付款单
*/
@Path("/fund/book/payOrder")
public class BookPayOrderController extends BaseController {

	@Inject
	private BookPayOrderService payOrderService;
	@Inject
	private PurchaseOrderService purchaseOrderService;

	/**
	* 首页
	*/
	@Permission(Permissions.fund_book_payOrder)
	public void index() {

	}
	
	/**
	* 列表
	*/
	@Permission(Permissions.fund_book_payOrder)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Kv condKv = conditions();
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<TraderPayOrder> page = payOrderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
		setAttr("hideDisableFlag", getBoolean("hide_disable_flag"));
		setAttr("showAuditFlag", getBoolean("show_audit_flag"));
		setAttrCommon();
	}

	/**
	* 草稿单
	*/
	@Permission(Permissions.fund_book_payOrder)
	public void draftIndex() {
		draftList();
	}
	/**
	* 草稿单列表
	*/
	@Permission(Permissions.fund_book_payOrder)
	public void draftList() {
		int pageNumber = getInt("pageNumber", 1);
		Integer supplierInfoId = getInt("supplier_info_id");
		pageSize = getPageSize();
		Kv condKv = Kv.create();
		condKv.set("make_man_id", getAdminId()); // 只显示当前用户的草稿单
		condKv.set("supplier_info_id", supplierInfoId);
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<TraderPayOrder> page = payOrderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
	}
	/**
	* 草稿单列表,JSON格式
	*/
	@Permission(Permissions.fund_book_payOrder)
	public void draftListByJson() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer supplierInfoId = getInt("supplier_info_id");
		Integer tenantStoreId = getInt("tenant_store_id");
		Kv condKv = Kv.create();
		condKv.set("tenant_store_id", tenantStoreId);
		condKv.set("supplier_info_id", supplierInfoId);
		condKv.set("make_man_id", getAdminId()); // 只显示当前用户的草稿单
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<TraderPayOrder> page = payOrderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		renderJson(Ret.ok().set("data", page));
	}
	

	/**
	* 查看
	*/
	@Permission(Permissions.fund_book_payOrder_show)
	public void show() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TraderPayOrder payOrder = TraderPayOrder.dao.findById(id);
		if(payOrder == null) {
			renderError(404);
			return;
		}
		setAttrCommon();
		setAttr("payOrder", payOrder);
	}
	

	/**
	* 添加
	*/
	@Permission(Permissions.fund_book_payOrder_create)
	public void add() {
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<TraderPayOrder> page = payOrderService.paginate(null, null, condKv, 1, 1);
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttrCommon();
	}

	/**
	* 新增
	*/
	@Permission(Permissions.fund_book_payOrder_create)
	public void create() {
		String responseJson = TraderCenterApi.requestApi("/fund/book/payOrder/create", getAdminId(), getParaMap());
		renderJson(responseJson);
	}

	/**
	* 编辑
	*/
	@Permission(Permissions.fund_book_payOrder_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TraderPayOrder payOrder = TraderPayOrder.dao.findById(id);
		if(payOrder == null) {
			renderError(404);
			return;
		}
		Page<PurchaseOrder> purchaseOrderPage = getPurchaseOrderList(payOrder);
		setAttr("payOrder", payOrder);
		setAttr("purchaseOrderPage", purchaseOrderPage);
		setAttrCommon();
		
	}
	
	/**
	* 编辑草稿单
	*/
	@Permission(Permissions.fund_book_payOrder_update)
	public void editDraft() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TraderPayOrder payOrder = TraderPayOrder.dao.findById(id);
		if(payOrder == null) {
			renderError(404);
			return;
		}
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<TraderPayOrder> page = payOrderService.paginate(null, null, condKv, 1, 1);
		
		Page<PurchaseOrder> purchaseOrderPage = getPurchaseOrderList(payOrder);
		
		setAttr("purchaseOrderPage", purchaseOrderPage);
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttr("payOrder", payOrder);
		setAttrCommon();
		
	}
	
	/**
	* 修改
	*/
	@Permission(Permissions.fund_book_payOrder_update)
	public void update() {
		String responseJson = TraderCenterApi.requestApi("/fund/book/payOrder/update", getAdminId(), getParaMap());
		renderJson(responseJson);
	}


	/**
	* 停用
	*/
	@Permission(Permissions.fund_book_payOrder_disable)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		String responseJson = TraderCenterApi.requestApi("/fund/book/payOrder/disable", getAdminId(), getParaMap());
		renderJson(responseJson);
	}
	
	/**
	* 审核
	*/
	@Permission(Permissions.fund_book_payOrder_audit)
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
	@Permission(Permissions.fund_book_payOrder_audit)
	public void auditCreate() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		String responseJson = TraderCenterApi.requestApi("/fund/book/payOrder/audit", getAdminId(), getParaMap());
		renderJson(responseJson);
	}
	
	/**
	* 删除
	*/
	@Permission(Permissions.fund_book_payOrder_update)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = payOrderService.delete(id);

		renderJson(ret);
	}
	
	/**
	* 待核销的销售单列表
	*/
	@Permission(Permissions.fund_book_payOrder)
	public void purchaseOrderList() {
		
		TraderPayOrder payOrder = TraderPayOrder.dao.findById(getInt("id"));
		
		Page<PurchaseOrder> purchaseOrderPage = getPurchaseOrderList(payOrder);
		
		setAttr("payOrder", payOrder);
		setAttr("purchaseOrderPage", purchaseOrderPage);
		keepPara("order_time_btn");
	}

	/**
	 * 获取待核销的销售单
	 * @param payOrder
	 * @return
	 */
	@NotAction
	private Page<PurchaseOrder> getPurchaseOrderList(TraderPayOrder payOrder) {
		Integer supplierInfoId = getInt("supplier_info_id");
		if(supplierInfoId == null && payOrder != null) {
			supplierInfoId = payOrder.getSupplierInfoId();
		}
		Kv condKv = Kv.create();
		condKv.set("supplier_info_id", supplierInfoId);
		condKv.set("order_status", OrderStatusEnum.normal.getValue());
		condKv.set("audit_status", AuditStatusEnum.pass.getValue());
		
		ConditionFilter rejectFilter = new ConditionFilter();
		rejectFilter.setOperator(Operator.neq);
		rejectFilter.setValue(RejectTypeEnum.all.getValue());
		condKv.set("reject_type", rejectFilter);
		
		ConditionFilter filter = new ConditionFilter();
		filter.setOperator(Operator.neq);
		filter.setValue(OrderPayStatusEnum.finish.getValue());
		condKv.set("pay_status", filter);
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<PurchaseOrder> page = purchaseOrderService.paginate(startTime, endTime, condKv, 1, maxPageSize);
		
		if(payOrder != null) {
			List<TraderPayOrderRef> refList = payOrder.getOrderRefList();
			for (TraderPayOrderRef orderRef : refList) {
				boolean isExist = false;
				for (PurchaseOrder purchaseOrder : page.getList()) {
					if(purchaseOrder.getId().intValue() == orderRef.getPurchaseOrderId().intValue()) {
						isExist = true;
						break;
					}
				}
				if(!isExist) {
					page.getList().add(0, orderRef.getPurchaseOrder());
				}
			}
		}
		return page;
	}
	
	
	/**
	 * 公共数据
	 */
	private void setAttrCommon() {
		JSONObject printConfirmConfig = TenantConfig.dao.findJsonByKeyCahce(TenantConfigEnum.create_order_print_confirm);
		setAttr("orderPrintConfig", printConfirmConfig);
		setAttr("orderAuditFlag", TraderPayOrder.dao.findAuditConfig()); // 是否审核
	}
	
	
	/**
	* 显示打印模板
	*/
	@Permission(Permissions.fund_book_payOrder)
	@Before(Tx.class)
	public void showPrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		TraderPayOrder traderPayOrder = TraderPayOrder.dao.findById(id);
		if(traderPayOrder == null) {
			renderError(404);
			return;
		}
		List<TenantPrintTemplate> templateList = traderPayOrder.getPrintTemplateList();
		setAttr("templateList", templateList);
		setAttr("payOrder", traderPayOrder);
	}
	
	/**
	 * 打印或预览
	 */
	@Permission(Permissions.fund_book_payOrder)
	@Before(Tx.class)
	public void print() {
		Integer id = getInt("id");
		Integer templateId = getInt("template_id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		TraderPayOrder traderPayOrder = TraderPayOrder.dao.findById(id);
		if(traderPayOrder == null) {
			renderError(404);
			return;
		}
		TenantPrintTemplate printTemplate;
		if(templateId == null || templateId <= 0) {
			printTemplate = traderPayOrder.getPrintDefaultTemplate();
		} else {
			printTemplate = TenantPrintTemplate.dao.findById(templateId);
		}
		if(printTemplate == null) {
			renderJson(Ret.fail("打印模板不存在，请先设置打印模板"));
			return;
		}
		Ret ret = Ret.ok();
		ret.set("tpl_content", printTemplate.getContent());
		ret.set("json_data", traderPayOrder.getPrintData());
		ret.set("order", traderPayOrder);
		
		renderJson(ret);
	}
	
	/**
	 * 更新打印次数
	 */
	@Permission(Permissions.fund_book_payOrder)
	@Before(Tx.class)
	public void updatePrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		TraderPayOrder traderPayOrder = TraderPayOrder.dao.findById(id);
		if(traderPayOrder == null) {
			renderError(404);
			return;
		}
		traderPayOrder.setPrintCount(traderPayOrder.getPrintCount()+1);
		traderPayOrder.setUpdatedAt(new Date());
		traderPayOrder.update();
		renderJson(Ret.ok());
	}
	
	/**
	 * 导出
	 */
	@Permission(Permissions.fund_book_payOrder_export)
	public void export() {
		Kv condKv = conditions();
		String startTime = get("start_time");
		String endTime = get("end_time");
		Ret ret = payOrderService.export(getAdminId(), startTime, endTime, condKv);
		renderJson(ret);
	}
	
	/**
	 * @return
	 */
	private Kv conditions() {
		Integer handlerId = getInt("handler_id");
		Integer supplierInfoId = getInt("supplier_info_id");
		Boolean hideDisableFlag = getBoolean("hide_disable_flag"); // 隐藏作废单据
		Boolean showAuditFlag = getBoolean("show_audit_flag"); // 只显示待审核单据
		
		String keyword = get("keyword");
		Kv condKv = Kv.create();
		conditionFilterStore(condKv, Permissions.fund_book); // 添加门店过滤条件
		condKv.set("handler_id", handlerId);
		condKv.set("supplier_info_id", supplierInfoId);
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
		return condKv;
	}

}