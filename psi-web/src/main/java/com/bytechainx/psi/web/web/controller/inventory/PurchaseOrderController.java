package com.bytechainx.psi.web.web.controller.inventory;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FundTypeEnum;
import com.bytechainx.psi.common.EnumConstant.OrderPayStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.model.PurchaseOrder;
import com.bytechainx.psi.common.model.PurchaseOrderCost;
import com.bytechainx.psi.common.model.PurchaseOrderFee;
import com.bytechainx.psi.common.model.PurchaseOrderFund;
import com.bytechainx.psi.common.model.PurchaseOrderGoods;
import com.bytechainx.psi.common.model.SupplierInfo;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantPrintTemplate;
import com.bytechainx.psi.purchase.service.PurchaseOrderService;
import com.bytechainx.psi.web.epc.TraderEventProducer;
import com.bytechainx.psi.web.epc.event.inventory.PurchaseOrderEvent;
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
	@Inject
	private TraderEventProducer traderEventProducer;

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
		
		Kv condKv = Kv.create();
		condKv.set("supplier_info_id", supplierInfoId);
		
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
		PurchaseOrder purchaseOrder = getModel(PurchaseOrder.class, "", true);
		List<PurchaseOrderGoods> orderGoodList = new ArrayList<>();
		List<PurchaseOrderFund> orderFundList = new ArrayList<>();
		List<PurchaseOrderFee> orderFeeList = new ArrayList<>();
		List<PurchaseOrderCost> orderCostList = new ArrayList<>();
		
		parserParams(purchaseOrder, orderGoodList, orderFundList, orderFeeList, orderCostList);
		
		Ret ret = traderEventProducer.request(getAdminId(), new PurchaseOrderEvent("create"), purchaseOrder, orderGoodList, orderFundList, orderFeeList, orderCostList);
		
		renderJson(ret);
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
		PurchaseOrder purchaseOrder = getModel(PurchaseOrder.class, "", true);
		List<PurchaseOrderGoods> orderGoodList = new ArrayList<>();
		List<PurchaseOrderFund> orderFundList = new ArrayList<>();
		List<PurchaseOrderFee> orderFeeList = new ArrayList<>();
		List<PurchaseOrderCost> orderCostList = new ArrayList<>();
		
		parserParams(purchaseOrder, orderGoodList, orderFundList, orderFeeList, orderCostList);
		
		Ret ret = traderEventProducer.request(getAdminId(), new PurchaseOrderEvent("update"), purchaseOrder, orderGoodList, orderFundList, orderFeeList, orderCostList);
		
		renderJson(ret);
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
		List<Integer> ids = new ArrayList<>();
		ids.add(id);
		
		Ret ret = traderEventProducer.request(getAdminId(), new PurchaseOrderEvent("disable"), ids);
		
		renderJson(ret);
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
		List<Integer> ids = new ArrayList<>();
		ids.add(id);
		
		AuditStatusEnum auditStatus = AuditStatusEnum.getEnum(getInt("audit_status"));
		if(auditStatus == null) {
			renderJson(Ret.fail("审核状态不正确"));
			return;
		}
		String auditDesc = get("audit_desc");
		Ret ret = traderEventProducer.request(getAdminId(), new PurchaseOrderEvent("audit"), ids, auditStatus, auditDesc, getAdminId());
		
		renderJson(ret);
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
	
	private void parserParams(PurchaseOrder purchaseOrder, List<PurchaseOrderGoods> orderGoodList, List<PurchaseOrderFund> orderFundList, List<PurchaseOrderFee> orderFeeList, List<PurchaseOrderCost> orderCostList) {
		Integer[] goodsIds = getParaValuesToInt("goods_id");
		Integer[] goodsInfoIds = getParaValuesToInt("goods_info_id");
		String[] goodsSpecIds = getParaValues("goods_spec_id");
		Integer[] unitIds = getParaValuesToInt("goods_unit_id");
		
		String[] goodsPrices = getParaValues("goods_price");
		String[] goodsDiscounts = getParaValues("goods_discount");
		String[] goodsDiscountAmounts = getParaValues("goods_discount_amount");
		String[] goodsBuyNumbers = getParaValues("goods_buy_number");
		String[] goodsTotalAmounts = getParaValues("goods_total_amount");
		String[] goodsRemarks = getParaValues("goods_remark");
		
		Integer[] orderFundIds = getParaValuesToInt("order_fund_id");
		Integer[] orderFundPayTypes = getParaValuesToInt("order_fund_pay_type");
		Integer[] balanceAccountIds = getParaValuesToInt("order_fund_balance_account_id");
		String[] orderFundAmounts = getParaValues("order_fund_amount");
		String[] orderFundPayTimes = getParaValues("order_fund_pay_time");
		
		String[] feeIds = getParaValues("fee_id"); // 其他费用
		String[] feeAmounts = getParaValues("fee_amount");
		
		String[] costIds = getParaValues("cost_id"); // 成本费用
		String[] costAmounts = getParaValues("cost_amount");
		
		for (int index = 0; index < goodsInfoIds.length; index++) {
			if(goodsInfoIds[index] == null) {
				continue;
			}
			PurchaseOrderGoods goods = new PurchaseOrderGoods();
			if(goodsIds != null) {
				goods.setId(goodsIds[index]);
			}
			
			goods.setAmount(new BigDecimal(goodsTotalAmounts[index]));
			goods.setPrice(new BigDecimal(goodsPrices[index]));
			goods.setBuyNumber(new BigDecimal(goodsBuyNumbers[index]));
			goods.setDiscount(new BigDecimal(goodsDiscounts[index]));
			goods.setDiscountAmount(new BigDecimal(goodsDiscountAmounts[index]));
			goods.setGoodsInfoId(goodsInfoIds[index]);
			if(goodsRemarks != null) {
				goods.setRemark(goodsRemarks[index]);
			}
			goods.setUnitId(unitIds[index]);
			
			goods.setSpec1Id(0);
			goods.setSpecOption1Id(0);
			goods.setSpec2Id(0);
			goods.setSpecOption2Id(0);
			goods.setSpec3Id(0);
			goods.setSpecOption3Id(0);
			
			if(goodsSpecIds != null) {
				String specstring = goodsSpecIds[index];// 多个规格使用逗号隔开,格式：规格ID:规格值ID, 如：11:22|33:44
				String[] specList = StringUtils.split(specstring, "|");
				if(specList != null &&  specList.length > 0) {
					String[] spec = StringUtils.split(specList[0], ":"); 
					goods.setSpec1Id(Integer.parseInt((spec[0])));
					goods.setSpecOption1Id(Integer.parseInt((spec[1])));
					
				}
				if(specList != null && specList.length > 1) {
					String[] spec = StringUtils.split(specList[1], ":"); 
					goods.setSpec2Id(Integer.parseInt((spec[0])));
					goods.setSpecOption2Id(Integer.parseInt((spec[1])));
				}
				if(specList != null && specList.length > 2) {
					String[] spec = StringUtils.split(specList[2], ":"); 
					goods.setSpec3Id(Integer.parseInt((spec[0])));
					goods.setSpecOption3Id(Integer.parseInt((spec[1])));
				}
			}
			
			orderGoodList.add(goods);
		}
		if(balanceAccountIds != null && balanceAccountIds.length > 0) {
			for (int index = 0; index < balanceAccountIds.length; index++) {
				if(orderFundAmounts ==null || orderFundAmounts[index] == null || StringUtils.isEmpty(orderFundAmounts[index]) || new BigDecimal(orderFundAmounts[index]).compareTo(BigDecimal.ZERO) <= 0) {
					continue;
				}
				PurchaseOrderFund fund = new PurchaseOrderFund();
				if(orderFundIds != null) {
					fund.setId(orderFundIds[index]);
				}
				Integer accountId = balanceAccountIds[index];
				Integer payType = null;
				if(orderFundPayTypes != null) {
					payType = orderFundPayTypes[index];
				}
				if(payType == null || accountId == 0) { // 新的资金支付没有这个类型
					if(accountId == 0) { //账户ID为0，则表示是余额扣款
						fund.setPayType(FundTypeEnum.balance.getValue());
						SupplierInfo supplierInfo = purchaseOrder.getSupplierInfo();
						fund.setBalanceAccountId(supplierInfo.getTraderBookAccountId());
						
					} else {
						fund.setPayType(FundTypeEnum.cash.getValue());
						fund.setBalanceAccountId(accountId); // 本单收款为结算帐户ID，余款扣款为往来帐户ID，核销清账为收款单ID
					}
				} else {
					fund.setPayType(payType);
					fund.setBalanceAccountId(accountId);
				}
				
				fund.setAmount(new BigDecimal(orderFundAmounts[index]));
				
				if(orderFundPayTimes != null && orderFundPayTimes[index] != null && StringUtils.isNotEmpty(orderFundPayTimes[index])) {
					fund.setPayTime(DateUtil.getDayDate(orderFundPayTimes[index]));
				} else {
					fund.setPayTime(new Date());
				}
				orderFundList.add(fund);
			}
		}
		
		if(feeIds != null && feeIds.length > 0) {
			for (int index = 0; index < feeIds.length; index++) {
				if(feeAmounts[index] == null || StringUtils.isEmpty(feeAmounts[index])) {
					continue;
				}
				PurchaseOrderFee fee = new PurchaseOrderFee();
				fee.setAmount(new BigDecimal(feeAmounts[index]));
				fee.setTraderFundType(Integer.parseInt(feeIds[index]));
				
				orderFeeList.add(fee);
			}
		}
		
		if(costIds != null && costIds.length > 0) {
			for (int index = 0; index < costIds.length; index++) {
				if(costAmounts[index] == null || StringUtils.isEmpty(costAmounts[index])) {
					continue;
				}
				PurchaseOrderCost cost = new PurchaseOrderCost();
				cost.setAmount(new BigDecimal(costAmounts[index]));
				cost.setTraderFundType(Integer.parseInt(costIds[index]));
				
				orderCostList.add(cost);
			}
		}
		
		purchaseOrder.setOrderImg(StringUtils.join(getParaValues("order_imgs"), ","));
		purchaseOrder.setMakeManId(getAdminId());
		purchaseOrder.setLastManId(getAdminId());
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