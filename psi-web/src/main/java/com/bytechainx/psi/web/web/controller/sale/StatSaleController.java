package com.bytechainx.psi.web.web.controller.sale;


import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.CustomerCategory;
import com.bytechainx.psi.common.model.CustomerInfo;
import com.bytechainx.psi.common.model.SaleOrder;
import com.bytechainx.psi.common.model.SaleOrderGoods;
import com.bytechainx.psi.sale.service.CustomerCategoryService;
import com.bytechainx.psi.sale.service.SaleOrderService;
import com.bytechainx.psi.sale.service.StatSaleService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Page;


/**
* 销售统计
*/
@Path("/sale/stat/sale")
public class StatSaleController extends BaseController {

	@Inject
	private StatSaleService statSaleService;
	@Inject
	private SaleOrderService saleOrderService;
	@Inject
	private CustomerCategoryService customerCategoryService;
	
	/**
	* 首页
	*/
	@Permission(Permissions.sale_stat_sale)
	public void index() {
		Page<CustomerCategory> customerCategoryPage = customerCategoryService.paginate(1, maxPageSize);
		setAttr("customerCategoryPage", customerCategoryPage);
	}

	/**
	* 按客户列表
	*/
	@Permission(Permissions.sale_stat_sale)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer customerInfoId = getInt("customer_info_id");
		Integer customerCategoryId = getInt("customer_category_id");
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<SaleOrder> page = statSaleService.paginateByCustomer(customerCategoryId, customerInfoId, startTime, endTime, pageNumber, pageSize);
		SaleOrder sumOrder = statSaleService.sumByCustomer(customerCategoryId, customerInfoId, startTime, endTime);
		setAttr("page", page);
		setAttr("sumOrder", sumOrder);
	}
	
	/**
	* 查看客户销售单据详情
	*/
	@Permission(Permissions.sale_stat_sale_show)
	public void show() {
		Integer customerInfoId = getInt("customer_info_id");
		
		Integer customerCategoryId = getInt("customer_category_id");
		if(customerInfoId == null || customerInfoId <= 0) {
			renderError(404);
			return;
		}
		CustomerInfo customerInfo = CustomerInfo.dao.findById(customerInfoId);
		if(customerInfo == null) {
			renderError(404);
			return;
		}
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<SaleOrder> sumOrder = statSaleService.paginateByCustomer(customerCategoryId, customerInfoId, startTime, endTime, 1, pageSize);
		if(sumOrder.getTotalRow() > 0) {
			setAttr("sumOrder", sumOrder.getList().get(0));
		}
		
		setAttr("customerInfo", customerInfo);
		setAttr("startTime", startTime);
		setAttr("endTime", endTime);
	}
	
	/**
	* 查看客户销售单据详情
	*/
	@Permission(Permissions.sale_stat_sale_show)
	public void showList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer customerInfoId = getInt("customer_info_id");
		Kv condKv = Kv.create();
		conditionFilterStore(condKv, Permissions.sale_sale);
		condKv.set("customer_info_id", customerInfoId);
		condKv.set("order_status", OrderStatusEnum.normal.getValue());
		condKv.set("audit_status", AuditStatusEnum.pass.getValue());
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<SaleOrder> page = saleOrderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		
		setAttr("page", page);
	}
	
	/**
	* 查看客户销售商品详情
	*/
	@Permission(Permissions.sale_stat_sale_show)
	public void showGoods() {
		show();
	}
	
	/**
	* 查看客户销售商品详情
	*/
	@Permission(Permissions.sale_stat_sale_show)
	public void showGoodsList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer customerInfoId = getInt("customer_info_id");
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<SaleOrderGoods> page = statSaleService.paginateByCustomerGoods(customerInfoId, startTime, endTime, pageNumber, pageSize);
		
		setAttr("page", page);
	}
	
}