package com.bytechainx.psi.web.web.controller.inventory;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

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
import com.bytechainx.psi.common.kit.StrUtil;
import com.bytechainx.psi.common.model.PurchaseOrder;
import com.bytechainx.psi.common.model.PurchaseOrderGoods;
import com.bytechainx.psi.common.model.PurchaseRejectOrder;
import com.bytechainx.psi.common.model.PurchaseRejectOrderGoods;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantPrintTemplate;
import com.bytechainx.psi.purchase.service.PurchaseRejectOrderService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 进货退货单
*/
@Path("/inventory/purchase/rejectOrder")
public class PurchaseRejectOrderController extends BaseController {


	@Inject
	private PurchaseRejectOrderService rejectOrderService;

	/**
	* 首页
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder)
	public void index() {
		
	}

	/**
	* 列表
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Kv condKv = conditions();
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<PurchaseRejectOrder> page = rejectOrderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
		setAttr("hideDisableFlag", getBoolean("hide_disable_flag"));
		setAttr("showAuditFlag", getBoolean("show_audit_flag"));
		setAttr("hidePayFlag", getBoolean("hide_pay_flag"));
		setAttrCommon();
	}

	/**
	* 草稿单
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder)
	public void draftIndex() {
		draftList();
	}
	/**
	* 草稿单列表
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder)
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
		
		Page<PurchaseRejectOrder> page = rejectOrderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
	}
	/**
	* 草稿单列表,JSON格式
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder)
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
		
		Page<PurchaseRejectOrder> page = rejectOrderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		renderJson(Ret.ok().set("data", page));
	}
	

	/**
	* 查看
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder_show)
	public void show() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		PurchaseRejectOrder purchaseRejectOrder = PurchaseRejectOrder.dao.findById(id);
		if(purchaseRejectOrder == null) {
			renderError(404);
			return;
		}
		setAttrCommon();
		setAttr("purchaseRejectOrder", purchaseRejectOrder);
	}
	
	/**
	* 收入款日志
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder_show)
	public void showPayLog() {
		show();
	}
	
	/**
	* 单据操作日志
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder_show)
	public void showOrderLog() {
		show();
	}


	/**
	* 添加
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder_create)
	public void add() {
		String orderIds = get("order_ids"); // 进货订单，订单转进货单，多个逗号隔开
		PurchaseRejectOrder purchaseRejectOrder = transferBookOrder(orderIds);
		
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<PurchaseRejectOrder> page = rejectOrderService.paginate(null, null, condKv, 1, 1);
		
		setAttr("purchaseRejectOrder", purchaseRejectOrder);
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttrCommon();
		
	}

	/**
	* 新增
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder_create)
	public void create() {
		String responseJson = TraderCenterApi.requestApi("/inventory/purchase/rejectOrder/create", getAdminId(), getParaMap());
		renderJson(responseJson);
	}

	/**
	* 编辑
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		PurchaseRejectOrder purchaseRejectOrder = PurchaseRejectOrder.dao.findById(id);
		if(purchaseRejectOrder == null) {
			renderError(404);
			return;
		}
		setAttr("purchaseRejectOrder", purchaseRejectOrder);
		setAttrCommon();
		
	}
	
	/**
	* 编辑草稿单
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder_update)
	public void editDraft() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		PurchaseRejectOrder purchaseRejectOrder = PurchaseRejectOrder.dao.findById(id);
		if(purchaseRejectOrder == null) {
			renderError(404);
			return;
		}
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<PurchaseRejectOrder> page = rejectOrderService.paginate(null, null, condKv, 1, 1);
		
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttr("purchaseRejectOrder", purchaseRejectOrder);
		setAttrCommon();
		
	}
	
	/**
	* 修改
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder_update)
	public void update() {
		String responseJson = TraderCenterApi.requestApi("/inventory/purchase/rejectOrder/update", getAdminId(), getParaMap());
		renderJson(responseJson);
	}


	/**
	* 停用
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder_disable)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		String responseJson = TraderCenterApi.requestApi("/inventory/purchase/rejectOrder/disable", getAdminId(), getParaMap());
		renderJson(responseJson);
	}
	
	/**
	* 审核
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder_audit)
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
	@Permission(Permissions.inventory_purchase_rejectOrder_audit)
	public void auditCreate() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		String responseJson = TraderCenterApi.requestApi("/inventory/purchase/rejectOrder/audit", getAdminId(), getParaMap());
		renderJson(responseJson);
	}
	
	/**
	* 删除
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder_update)
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
		JSONObject printConfirmConfig = TenantConfig.dao.findJsonByKeyCahce(TenantConfigEnum.create_order_print_confirm);
		setAttr("orderPrintConfig", printConfirmConfig);
		setAttr("orderGoodsDiscountFlag", Boolean.parseBoolean(orderGoodsDiscountConfig.getAttrValue()));
		setAttr("orderAuditFlag", PurchaseRejectOrder.dao.findAuditConfig()); // 是否审核
		setAttr("orderRows", PurchaseRejectOrder.dao.findRowsConfig()); // 默认表格行数，后期要做成配置
		setAttr("feeList", PurchaseRejectOrder.dao.findFeeConfig()); // 其他费用配置
		setAttr("costList", PurchaseRejectOrder.dao.findCostConfig()); // 成本支出配置
	
		TenantConfig orderFundPaytimeConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.order_fund_paytime);
		setAttr("orderFundPaytimeFlag", Boolean.parseBoolean(orderFundPaytimeConfig.getAttrValue()));
	}
	
	/**
	 * 订单转进货单
	 * @param orderIds
	 */
	private PurchaseRejectOrder transferBookOrder(String orderIds) {
		if(StringUtils.isEmpty(orderIds)) {
			return null;
		}
		String[] orderIdsString = StringUtils.split(orderIds, ",");
		PurchaseRejectOrder purchaseRejectOrder = null;
		List<PurchaseRejectOrderGoods> rejectOrderGoodsList = new ArrayList<>();
		for (String idstring : orderIdsString) {
			if(StringUtils.isEmpty(idstring)) {
				continue;
			}
			Integer bookOrderId = Integer.parseInt(idstring);
			PurchaseOrder purchaseOrder = PurchaseOrder.dao.findById(bookOrderId);
			if(purchaseRejectOrder == null) {
				purchaseRejectOrder = new PurchaseRejectOrder();
				purchaseRejectOrder._setOrPut(purchaseOrder);
				purchaseRejectOrder.remove("id");
			} else {
				purchaseRejectOrder.setGoodsAmount(purchaseRejectOrder.getGoodsAmount().add(purchaseOrder.getGoodsAmount()));
				purchaseRejectOrder.setDiscountAmount(purchaseRejectOrder.getDiscountAmount().add(purchaseOrder.getDiscountAmount()));
				purchaseRejectOrder.setAmount(purchaseRejectOrder.getAmount().add(purchaseOrder.getAmount()));
				purchaseRejectOrder.setDiscount(purchaseRejectOrder.getDiscountAmount().divide(purchaseRejectOrder.getGoodsAmount(), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100).setScale(-2)));
			}
			for(PurchaseOrderGoods purchaseOrderGoods : purchaseOrder.getOrderGoodsList()) {
				if(purchaseOrderGoods.getBuyNumber().compareTo(purchaseOrderGoods.getRejectAmount()) <= 0) { // 全部退了，不能再退了。
					continue;
				}
				PurchaseRejectOrderGoods purchaseRejectOrderGoods = new PurchaseRejectOrderGoods();
				purchaseRejectOrderGoods._setOrPut(purchaseOrderGoods);
				purchaseRejectOrderGoods.setBuyNumber(purchaseOrderGoods.getBuyNumber().subtract(purchaseOrderGoods.getRejectNumber())); // 只能转剩余未退数量
				purchaseRejectOrderGoods.setAmount(purchaseRejectOrderGoods.getBuyNumber().multiply(purchaseRejectOrderGoods.getPrice()));
				purchaseRejectOrderGoods.remove("id");
				
				rejectOrderGoodsList.add(purchaseRejectOrderGoods);
			}
		}
		if(purchaseRejectOrder != null) {
			purchaseRejectOrder.setOrderGoodsList(rejectOrderGoodsList);
			purchaseRejectOrder.setPurchaseOrderId(StrUtil.beforeAfterCleanComma(orderIds));
		}
		return purchaseRejectOrder;
	}
	
	
	/**
	* 显示打印模板
	*/
	@Permission(Permissions.inventory_purchase_rejectOrder)
	@Before(Tx.class)
	public void showPrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		PurchaseRejectOrder purchaseRejectOrder = PurchaseRejectOrder.dao.findById(id);
		if(purchaseRejectOrder == null) {
			renderError(404);
			return;
		}
		List<TenantPrintTemplate> templateList = purchaseRejectOrder.getPrintTemplateList();
		setAttr("templateList", templateList);
		setAttr("purchaseRejectOrder", purchaseRejectOrder);
	}
	
	/**
	 * 打印或预览
	 */
	@Permission(Permissions.inventory_purchase_rejectOrder)
	@Before(Tx.class)
	public void print() {
		Integer id = getInt("id");
		Integer templateId = getInt("template_id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		PurchaseRejectOrder purchaseRejectOrder = PurchaseRejectOrder.dao.findById(id);
		if(purchaseRejectOrder == null) {
			renderError(404);
			return;
		}
		TenantPrintTemplate printTemplate;
		if(templateId == null || templateId <= 0) {
			printTemplate = purchaseRejectOrder.getPrintDefaultTemplate();
		} else {
			printTemplate = TenantPrintTemplate.dao.findById(templateId);
		}
		if(printTemplate == null) {
			renderJson(Ret.fail("打印模板不存在，请先设置打印模板"));
			return;
		}
		Ret ret = Ret.ok();
		ret.set("tpl_content", printTemplate.getContent());
		ret.set("json_data", purchaseRejectOrder.getPrintData());
		ret.set("order", purchaseRejectOrder);
		
		renderJson(ret);
	}
	
	/**
	 * 更新打印次数
	 */
	@Permission(Permissions.inventory_purchase_rejectOrder)
	@Before(Tx.class)
	public void updatePrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		PurchaseRejectOrder purchaseRejectOrder = PurchaseRejectOrder.dao.findById(id);
		if(purchaseRejectOrder == null) {
			renderError(404);
			return;
		}
		purchaseRejectOrder.setPrintCount(purchaseRejectOrder.getPrintCount()+1);
		purchaseRejectOrder.setUpdatedAt(new Date());
		purchaseRejectOrder.update();
		renderJson(Ret.ok());
	}

	/**
	 * 导出
	 */
	@Permission(Permissions.inventory_purchase_rejectOrder_export)
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