package com.bytechainx.psi.web.web.controller.fund;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderPayStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.RejectTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.model.SaleOrder;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.model.TenantPrintTemplate;
import com.bytechainx.psi.common.model.TraderReceiptOrder;
import com.bytechainx.psi.common.model.TraderReceiptOrderFund;
import com.bytechainx.psi.common.model.TraderReceiptOrderRef;
import com.bytechainx.psi.fund.service.BookReceiptOrderService;
import com.bytechainx.psi.sale.service.SaleOrderService;
import com.bytechainx.psi.web.epc.TraderEventProducer;
import com.bytechainx.psi.web.epc.event.fund.BookReceiptOrderEvent;
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
* 收款单
*/
@Path("/fund/book/receiptOrder")
public class BookReceiptOrderController extends BaseController {

	@Inject
	private BookReceiptOrderService receiptOrderService;
	@Inject
	private SaleOrderService saleOrderService;
	@Inject
	private TraderEventProducer traderEventProducer;

	/**
	* 首页
	*/
	@Permission(Permissions.fund_book_receiptOrder)
	public void index() {
		
	}

	/**
	* 列表
	*/
	@Permission(Permissions.fund_book_receiptOrder)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Kv condKv = conditions();
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<TraderReceiptOrder> page = receiptOrderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
		setAttr("hideDisableFlag", getBoolean("hide_disable_flag"));
		setAttr("showAuditFlag", getBoolean("show_audit_flag"));
		setAttrCommon();
	}

	/**
	* 草稿单
	*/
	@Permission(Permissions.fund_book_receiptOrder)
	public void draftIndex() {
		draftList();
	}
	/**
	* 草稿单列表
	*/
	@Permission(Permissions.fund_book_receiptOrder)
	public void draftList() {
		int pageNumber = getInt("pageNumber", 1);
		Integer customerInfoId = getInt("customer_info_id");
		pageSize = getPageSize();
		Kv condKv = Kv.create();
		condKv.set("make_man_id", getAdminId()); // 只显示当前用户的草稿单
		condKv.set("customer_info_id", customerInfoId);
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<TraderReceiptOrder> page = receiptOrderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
	}
	/**
	* 草稿单列表,JSON格式
	*/
	@Permission(Permissions.fund_book_receiptOrder)
	public void draftListByJson() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer customerInfoId = getInt("customer_info_id");
		
		Kv condKv = Kv.create();
		condKv.set("customer_info_id", customerInfoId);
		condKv.set("make_man_id", getAdminId()); // 只显示当前用户的草稿单
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<TraderReceiptOrder> page = receiptOrderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		renderJson(Ret.ok().set("data", page));
	}
	

	/**
	* 查看
	*/
	@Permission(Permissions.fund_book_receiptOrder_show)
	public void show() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TraderReceiptOrder receiptOrder = TraderReceiptOrder.dao.findById(id);
		if(receiptOrder == null) {
			renderError(404);
			return;
		}
		setAttrCommon();
		setAttr("receiptOrder", receiptOrder);
	}
	

	/**
	* 添加
	*/
	@Permission(Permissions.fund_book_receiptOrder_create)
	public void add() {
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<TraderReceiptOrder> page = receiptOrderService.paginate(null, null, condKv, 1, 1);
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttrCommon();
	}

	/**
	* 新增
	*/
	@Permission(Permissions.fund_book_receiptOrder_create)
	public void create() {
		TraderReceiptOrder receiptOrder = getModel(TraderReceiptOrder.class, "", true);
		List<TraderReceiptOrderFund> orderFundList = new ArrayList<>();
		List<TraderReceiptOrderRef> orderRefList = new ArrayList<>();
		
		parserParams(receiptOrder, orderFundList, orderRefList);
		
		Ret ret = traderEventProducer.request(getAdminId(), new BookReceiptOrderEvent("create"), receiptOrder, orderFundList, orderRefList);
		
		renderJson(ret);
	}

	/**
	* 编辑
	*/
	@Permission(Permissions.fund_book_receiptOrder_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TraderReceiptOrder receiptOrder = TraderReceiptOrder.dao.findById(id);
		if(receiptOrder == null) {
			renderError(404);
			return;
		}
		Page<SaleOrder> saleOrderPage = getSaleOrderList(receiptOrder);
		setAttr("receiptOrder", receiptOrder);
		setAttr("saleOrderPage", saleOrderPage);
		setAttrCommon();
		
	}
	
	/**
	* 编辑草稿单
	*/
	@Permission(Permissions.fund_book_receiptOrder_update)
	public void editDraft() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		TraderReceiptOrder receiptOrder = TraderReceiptOrder.dao.findById(id);
		if(receiptOrder == null) {
			renderError(404);
			return;
		}
		Kv condKv = Kv.create();
		condKv.set("order_status", OrderStatusEnum.draft.getValue());
		condKv.set("make_man_id", getAdminId());
		Page<TraderReceiptOrder> page = receiptOrderService.paginate(null, null, condKv, 1, 1);
		
		Page<SaleOrder> saleOrderPage = getSaleOrderList(receiptOrder);
		
		setAttr("saleOrderPage", saleOrderPage);
		setAttr("draftCount", page.getTotalRow()); // 草稿单数量
		setAttr("receiptOrder", receiptOrder);
		setAttrCommon();
		
	}
	
	/**
	* 修改
	*/
	@Permission(Permissions.fund_book_receiptOrder_update)
	public void update() {
		TraderReceiptOrder receiptOrder = getModel(TraderReceiptOrder.class, "", true);
		List<TraderReceiptOrderFund> orderFundList = new ArrayList<>();
		List<TraderReceiptOrderRef> orderRefList = new ArrayList<>();
		
		parserParams(receiptOrder, orderFundList, orderRefList);
		
		Ret ret = traderEventProducer.request(getAdminId(), new BookReceiptOrderEvent("update"), receiptOrder, orderFundList, orderRefList);
		
		renderJson(ret);
	}


	/**
	* 停用
	*/
	@Permission(Permissions.fund_book_receiptOrder_disable)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		List<Integer> ids = new ArrayList<>();
		ids.add(id);
		
		Ret ret = traderEventProducer.request(getAdminId(), new BookReceiptOrderEvent("disable"), ids);
		
		renderJson(ret);
	}
	
	/**
	* 审核
	*/
	@Permission(Permissions.fund_book_receiptOrder_audit)
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
	@Permission(Permissions.fund_book_receiptOrder_audit)
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
		Ret ret = traderEventProducer.request(getAdminId(), new BookReceiptOrderEvent("audit"), ids, auditStatus, auditDesc, getAdminId());
		
		renderJson(ret);
	}
	
	/**
	* 删除
	*/
	@Permission(Permissions.fund_book_receiptOrder_update)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = receiptOrderService.delete(id);

		renderJson(ret);
	}
	
	/**
	* 待核销的销售单列表
	*/
	@Permission(Permissions.fund_book_receiptOrder)
	public void saleOrderList() {
		
		TraderReceiptOrder receiptOrder = TraderReceiptOrder.dao.findById(getInt("id"));
		
		Page<SaleOrder> saleOrderPage = getSaleOrderList(receiptOrder);
		
		setAttr("receiptOrder", receiptOrder);
		setAttr("saleOrderPage", saleOrderPage);
		keepPara("order_time_btn");
	}

	/**
	 * 获取待核销的销售单
	 * @param receiptOrder
	 * @return
	 */
	@NotAction
	private Page<SaleOrder> getSaleOrderList(TraderReceiptOrder receiptOrder) {
		Integer customerInfoId = getInt("customer_info_id");
		if(customerInfoId == null && receiptOrder != null) {
			customerInfoId = receiptOrder.getCustomerInfoId();
		}
		Kv condKv = Kv.create();
		condKv.set("customer_info_id", customerInfoId);
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
		
		Page<SaleOrder> page = saleOrderService.paginate(startTime, endTime, condKv, 1, maxPageSize);
		
		if(receiptOrder != null) {
			List<TraderReceiptOrderRef> refList = receiptOrder.getOrderRefList();
			for (TraderReceiptOrderRef orderRef : refList) {
				boolean isExist = false;
				for (SaleOrder saleOrder : page.getList()) {
					if(saleOrder.getId().intValue() == orderRef.getSaleOrderId().intValue()) {
						isExist = true;
						break;
					}
				}
				if(!isExist) {
					page.getList().add(0, orderRef.getSaleOrder());
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
		setAttr("orderAuditFlag", TraderReceiptOrder.dao.findAuditConfig()); // 是否审核
	}
	
	
	/**
	* 显示打印模板
	*/
	@Permission(Permissions.fund_book_receiptOrder)
	@Before(Tx.class)
	public void showPrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		TraderReceiptOrder traderReceiptOrder = TraderReceiptOrder.dao.findById(id);
		if(traderReceiptOrder == null) {
			renderError(404);
			return;
		}
		List<TenantPrintTemplate> templateList = traderReceiptOrder.getPrintTemplateList();
		setAttr("templateList", templateList);
		setAttr("receiptOrder", traderReceiptOrder);
	}
	
	/**
	 * 打印或预览
	 */
	@Permission(Permissions.fund_book_receiptOrder)
	@Before(Tx.class)
	public void print() {
		Integer id = getInt("id");
		Integer templateId = getInt("template_id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		TraderReceiptOrder traderReceiptOrder = TraderReceiptOrder.dao.findById(id);
		if(traderReceiptOrder == null) {
			renderError(404);
			return;
		}
		TenantPrintTemplate printTemplate;
		if(templateId == null || templateId <= 0) {
			printTemplate = traderReceiptOrder.getPrintDefaultTemplate();
		} else {
			printTemplate = TenantPrintTemplate.dao.findById(templateId);
		}
		if(printTemplate == null) {
			renderJson(Ret.fail("打印模板不存在，请先设置打印模板"));
			return;
		}
		Ret ret = Ret.ok();
		ret.set("tpl_content", printTemplate.getContent());
		ret.set("json_data", traderReceiptOrder.getPrintData());
		ret.set("order", traderReceiptOrder);
		
		renderJson(ret);
	}
	
	/**
	 * 更新打印次数
	 */
	@Permission(Permissions.fund_book_receiptOrder)
	@Before(Tx.class)
	public void updatePrint() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("单据ID不能为空"));
			return;
		}
		TraderReceiptOrder traderReceiptOrder = TraderReceiptOrder.dao.findById(id);
		if(traderReceiptOrder == null) {
			renderError(404);
			return;
		}
		traderReceiptOrder.setPrintCount(traderReceiptOrder.getPrintCount()+1);
		traderReceiptOrder.setUpdatedAt(new Date());
		traderReceiptOrder.update();
		renderJson(Ret.ok());
	}
	
	
	/**
	 * 导出
	 */
	@Permission(Permissions.fund_book_receiptOrder_export)
	public void export() {
		Kv condKv = conditions();
		String startTime = get("start_time");
		String endTime = get("end_time");
		Ret ret = receiptOrderService.export(getAdminId(), startTime, endTime, condKv);
		renderJson(ret);
	}
	
private void parserParams(TraderReceiptOrder receiptOrder, List<TraderReceiptOrderFund> orderFundList, List<TraderReceiptOrderRef> orderRefList) {
		
		Integer[] orderFundIds = getParaValuesToInt("order_fund_id");
		Integer[] balanceAccountIds = getParaValuesToInt("order_fund_balance_account_id");
		String[] orderFundAmounts = getParaValues("order_fund_amount");
		String[] orderFundFundTimes = getParaValues("order_fund_fund_time");
		
		Integer[] saleOrderIds = getParaValuesToInt("sale_order_id");
		String[] receiptAmounts = getParaValues("receipt_amount");
		String[] receiptDiscountAmounts = getParaValues("receipt_discount_amount");
		
		if(balanceAccountIds != null && balanceAccountIds.length > 0) {
			for (int index = 0; index < balanceAccountIds.length; index++) {
				if(orderFundAmounts == null || orderFundAmounts[index] == null || StringUtils.isEmpty(orderFundAmounts[index]) || new BigDecimal(orderFundAmounts[index]).compareTo(BigDecimal.ZERO) <= 0) {
					continue;
				}
				TraderReceiptOrderFund fund = new TraderReceiptOrderFund();
				if(orderFundIds != null) {
					fund.setId(orderFundIds[index]);
				}
				Integer accountId = balanceAccountIds[index];
				fund.setTraderBalanceAccountId(accountId);
				fund.setAmount(new BigDecimal(orderFundAmounts[index]));
				
				if(orderFundFundTimes != null && orderFundFundTimes[index] != null && StringUtils.isNotEmpty(orderFundFundTimes[index])) {
					fund.setFundTime(DateUtil.getDayDate(orderFundFundTimes[index]));
				} else {
					fund.setFundTime(new Date());
				}
				orderFundList.add(fund);
			}
		}
		
		if(receiptAmounts != null && receiptAmounts.length > 0) {
			for (int index = 0; index < receiptAmounts.length; index++) {
				if(receiptAmounts == null || receiptAmounts[index] == null || StringUtils.isEmpty(receiptAmounts[index]) || new BigDecimal(receiptAmounts[index]).compareTo(BigDecimal.ZERO) <= 0) {
					continue;
				}
				TraderReceiptOrderRef ref = new TraderReceiptOrderRef();
				Integer saleOrderId = saleOrderIds[index];
				ref.setSaleOrderId(saleOrderId);
				ref.setAmount(new BigDecimal(receiptAmounts[index]));
				if(receiptOrder != null) {
					ref.setTraderReceiptOrderId(receiptOrder.getId());
				}
				if(receiptDiscountAmounts == null || StringUtils.isEmpty(receiptDiscountAmounts[index])) {
					ref.setDiscountAmount(BigDecimal.ZERO);
				} else {
					ref.setDiscountAmount(new BigDecimal(receiptDiscountAmounts[index]));
				}
				orderRefList.add(ref);
			}
		}
		
		receiptOrder.setOrderImg(StringUtils.join(getParaValues("order_imgs"), ","));
		receiptOrder.setMakeManId(getAdminId());
		receiptOrder.setLastManId(getAdminId());
	}
	
	/**
	 * 查询条件
	 * @return
	 */
	private Kv conditions() {
		Integer handlerId = getInt("handler_id");
		Integer customerInfoId = getInt("customer_info_id");
		Boolean hideDisableFlag = getBoolean("hide_disable_flag"); // 隐藏作废单据
		Boolean showAuditFlag = getBoolean("show_audit_flag"); // 只显示待审核单据
		
		String keyword = get("keyword");
		Kv condKv = Kv.create();
		condKv.set("handler_id", handlerId);
		condKv.set("customer_info_id", customerInfoId);
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