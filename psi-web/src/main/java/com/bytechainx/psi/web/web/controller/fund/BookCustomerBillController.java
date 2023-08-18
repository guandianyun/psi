package com.bytechainx.psi.web.web.controller.fund;


import java.math.BigDecimal;
import java.util.List;

import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.dto.UserSession;
import com.bytechainx.psi.common.model.CustomerInfo;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TraderBookAccount;
import com.bytechainx.psi.common.model.TraderBookAccountLogs;
import com.bytechainx.psi.common.model.TraderCustomerReceivable;
import com.bytechainx.psi.fund.service.BookCustomerBillService;
import com.bytechainx.psi.web.epc.TraderEventProducer;
import com.bytechainx.psi.web.epc.event.sale.CustomerInfoEvent;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 客户对账
*/
@Path("/fund/book/customerBill")
public class BookCustomerBillController extends BaseController {

	@Inject
	private BookCustomerBillService customerBillService;
	@Inject
	private TraderEventProducer traderEventProducer;

	/**
	* 首页
	*/
	@Permission(Permissions.fund_book_customerBill)
	public void index() {
		
	}

	/**
	* 列表
	*/
	@Permission(Permissions.fund_book_customerBill)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Kv condKv = conditions();
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<TraderCustomerReceivable> page = customerBillService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		TraderCustomerReceivable customerReceivable = customerBillService.sumAmount(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
		setAttr("customerReceivable", customerReceivable);
	}

	/**
	* 查看
	*/
	@Permission(Permissions.fund_book_customerBill_show)
	public void show() {
		Integer customerInfoId = getInt("customer_info_id");
		if(customerInfoId == null || customerInfoId <= 0) {
			renderError(404);
			return;
		}
		CustomerInfo customerInfo = CustomerInfo.dao.findById(customerInfoId);
		if(customerInfo == null) {
			renderError(404);
			return;
		}
		Kv condKv = Kv.create();
		condKv.set("customer_info_id", customerInfoId);
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<TraderCustomerReceivable> customerReceivablePage = customerBillService.paginate(startTime, endTime, condKv, 1, 1);
		Page<TraderCustomerReceivable> page = customerBillService.paginateByList(customerInfoId, startTime, endTime, 1, pageSize);
		BigDecimal openBalance = customerBillService.getOpenBalance(customerInfoId+"", startTime);
		
		setAttr("openBalance", openBalance);
		if(customerReceivablePage.getTotalRow() > 0) {
			setAttr("customerReceivable", customerReceivablePage.getList().get(0));
		}
		setAttr("customerInfo", customerInfo);
		setAttr("page", page);
		setAttr("startTime", startTime);
		setAttr("endTime", endTime);
	}

	/**
	 * 对账明细
	 */
	@Permission(Permissions.fund_book_customerBill_show)
	public void showList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		String startTime = get("start_time");
		String endTime = get("end_time");
		Integer customerInfoId = getInt("customer_info_id");
		Page<TraderCustomerReceivable> page = customerBillService.paginateByList(customerInfoId, startTime, endTime, pageNumber, pageSize);
		BigDecimal openBalance = customerBillService.getOpenBalance(customerInfoId+"", startTime);
		
		setAttr("openBalance", openBalance);
		setAttr("page", page);
	}
	
	/**
	 * 期初调整
	 */
	@Permission(Permissions.fund_book_customerBill_openBalance)
	public void editOpenBalance() {
		
	}
	
	/**
	 * 期初调整
	 */
	@Permission(Permissions.fund_book_customerBill_openBalance)
	public void updateOpenBalance() {
		Integer customerId = getInt("customer_info_id");
		BigDecimal openBalance = get("open_balance") == null ? null : new BigDecimal(get("open_balance") ); // 期初欠款
		if(openBalance != null) {
			openBalance = openBalance.multiply(new BigDecimal(getInt("amount_type")));// 欠款为正数，余额为负数
		}
		Ret ret = traderEventProducer.request(getAdminId(), new CustomerInfoEvent("updateOpenBalance"), customerId, openBalance);
		renderJson(ret);
	}
	
	/**
	 * 期初调整日志
	 */
	@Permission(Permissions.fund_book_customerBill_openBalance)
	public void openBalanceLog() {
		Integer customerInfoId = getInt("customer_info_id");
		CustomerInfo customerInfo = CustomerInfo.dao.findById(customerInfoId);
		TraderBookAccount traderBookAccount = customerInfo.getTraderBookAccount();
		List<TraderBookAccountLogs> logs = TraderBookAccountLogs.dao.find("select * from trader_book_account_logs where trader_book_account_id = ? and fund_flow = ? order by id desc limit 5", traderBookAccount.getId(), FundFlowEnum.adjust.getValue());
		
		renderJson(Ret.ok().set("logs", logs).set("openBalance", traderBookAccount.getOpenBalance()));
	}
	
	
	/**
	 * 导出
	 */
	@Permission(Permissions.fund_book_customerBill_export)
	public void export() {
		Kv condKv = conditions();
		String startTime = get("start_time");
		String endTime = get("end_time");
		Ret ret = customerBillService.export(getAdminId(), startTime, endTime, condKv);
		renderJson(ret);
	}
	
	/**
	 * 导出明细
	 */
	public void exportList() {
		String startTime = get("start_time");
		String endTime = get("end_time");
		Integer customerInfoId = getInt("customer_info_id");
		
		Ret ret = customerBillService.exportList(getAdminId(), customerInfoId, startTime, endTime);
		renderJson(ret);
	}
	
	
	
	/**
	 * 查询条件
	 * @return
	 */
	private Kv conditions() {
		Integer customerInfoId = getInt("customer_info_id");
		Kv condKv = Kv.create();
		if(customerInfoId != null && customerInfoId > 0) {
			condKv.set("customer_info_id", customerInfoId);
		} else {
			TenantAdmin currentAdmin = getCurrentAdmin();
			UserSession session = getUserSession();
			if(!currentAdmin.isSuperAdmin() && !session.hasOper(Permissions.sensitiveData_customer_showSaleman)) {
				ConditionFilter filter = new ConditionFilter();
				filter.setOperator(Operator.in);
				filter.setValue("select id from customer_info where handler_id = "+currentAdmin.getId()+" or make_man_id = "+currentAdmin.getId());
				condKv.set("customer_info_id", filter);
			}
		}
		return condKv;
	}
}