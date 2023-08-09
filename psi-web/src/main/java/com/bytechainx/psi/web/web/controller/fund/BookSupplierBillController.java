package com.bytechainx.psi.web.web.controller.fund;


import java.math.BigDecimal;
import java.util.List;

import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.api.TraderCenterApi;
import com.bytechainx.psi.common.model.SupplierInfo;
import com.bytechainx.psi.common.model.TraderBookAccount;
import com.bytechainx.psi.common.model.TraderBookAccountLogs;
import com.bytechainx.psi.common.model.TraderSupplierPayable;
import com.bytechainx.psi.fund.service.BookSupplierBillService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 供应商对账
*/
@Path("/fund/book/supplierBill")
public class BookSupplierBillController extends BaseController {

	@Inject
	private BookSupplierBillService supplierBillService;

	/**
	* 首页
	*/
	@Permission(Permissions.fund_book_supplierBill)
	public void index() {
		
	}

	/**
	* 列表
	*/
	@Permission(Permissions.fund_book_supplierBill)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Kv condKv = conditions();
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<TraderSupplierPayable> page = supplierBillService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		TraderSupplierPayable supplierPayable = supplierBillService.sumAmount(startTime, endTime, condKv, pageNumber, pageSize);
		setAttr("page", page);
		setAttr("supplierPayable", supplierPayable);
	}

	/**
	* 查看
	*/
	@Permission(Permissions.fund_book_supplierBill_show)
	public void show() {
		Integer supplierInfoId = getInt("supplier_info_id");
		Integer tenantStoreId = getInt("tenant_store_id");
		if(supplierInfoId == null || supplierInfoId <= 0) {
			renderError(404);
			return;
		}
		SupplierInfo supplierInfo = SupplierInfo.dao.findById(supplierInfoId);
		if(supplierInfo == null) {
			renderError(404);
			return;
		}
		Kv condKv = Kv.create();
		condKv.set("tenant_store_id", tenantStoreId);
		condKv.set("supplier_info_id", supplierInfoId);
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<TraderSupplierPayable> supplierPayablePage = supplierBillService.paginate(startTime, endTime, condKv, 1, 1);
		Page<TraderSupplierPayable> page = supplierBillService.paginateByList(tenantStoreId, supplierInfoId, startTime, endTime, 1, pageSize);
		BigDecimal openBalance = supplierBillService.getOpenBalance(supplierInfoId, startTime);
		
		setAttr("openBalance", openBalance);
		if(supplierPayablePage.getTotalRow() > 0) {
			setAttr("supplierPayable", supplierPayablePage.getList().get(0));
		}
		setAttr("supplierInfo", supplierInfo);
		setAttr("page", page);
		setAttr("startTime", startTime);
		setAttr("endTime", endTime);
	}

	/**
	 * 对账明细
	 */
	public void showList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		String startTime = get("start_time");
		String endTime = get("end_time");
		Integer supplierInfoId = getInt("supplier_info_id");
		Integer tenantStoreId = getInt("tenant_store_id");
		Page<TraderSupplierPayable> page = supplierBillService.paginateByList(tenantStoreId, supplierInfoId, startTime, endTime, pageNumber, pageSize);
		BigDecimal openBalance = supplierBillService.getOpenBalance(supplierInfoId, startTime);
		
		setAttr("openBalance", openBalance);
		setAttr("page", page);
	}
	/**
	 * 期初调整
	 */
	@Permission(Permissions.fund_book_supplierBill_openBalance)
	public void editOpenBalance() {
		
	}
	
	/**
	 * 期初调整
	 */
	@Permission(Permissions.fund_book_supplierBill_openBalance)
	public void updateOpenBalance() {
		String responseJson = TraderCenterApi.requestApi("/inventory/supplier/info/updateOpenBalance", getAdminId(), getParaMap());
		renderJson(responseJson);
	}
	
	/**
	 * 期初调整日志
	 */
	@Permission(Permissions.fund_book_supplierBill_openBalance)
	public void openBalanceLog() {
		Integer supplierInfoId = getInt("supplier_info_id");
		SupplierInfo supplierInfo = SupplierInfo.dao.findById(supplierInfoId);
		TraderBookAccount traderBookAccount = supplierInfo.getTraderBookAccount();
		List<TraderBookAccountLogs> logs = TraderBookAccountLogs.dao.find("select * from trader_book_account_logs where trader_book_account_id = ? and fund_flow = ? order by id desc limit 5", traderBookAccount.getId(), FundFlowEnum.adjust.getValue());
		
		renderJson(Ret.ok().set("logs", logs).set("openBalance", traderBookAccount.getOpenBalance()));
	}
	
	
	/**
	 * 导出
	 */
	@Permission(Permissions.fund_book_supplierBill_export)
	public void export() {
		Kv condKv = conditions();
		String startTime = get("start_time");
		String endTime = get("end_time");
		Ret ret = supplierBillService.export(getAdminId(), startTime, endTime, condKv);
		renderJson(ret);
	}
	
	/**
	 * 导出明细
	 */
	public void exportList() {
		String startTime = get("start_time");
		String endTime = get("end_time");
		Integer supplierInfoId = getInt("supplier_info_id");
		Integer tenantStoreId = getInt("tenant_store_id");
		Ret ret = supplierBillService.exportList(getAdminId(), tenantStoreId, supplierInfoId, startTime, endTime);
		renderJson(ret);
	}
	
	
	
	/**
	 * 查询条件
	 * @return
	 */
	private Kv conditions() {
		Integer supplierInfoId = getInt("supplier_info_id");
		
		Kv condKv = Kv.create();
		conditionFilterStore(condKv, null); // 添加门店过滤条件
		condKv.set("supplier_info_id", supplierInfoId);
		return condKv;
	}
}