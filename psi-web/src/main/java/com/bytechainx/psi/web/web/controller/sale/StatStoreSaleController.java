package com.bytechainx.psi.web.web.controller.sale;


import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.SaleOrder;
import com.bytechainx.psi.common.model.SaleOrderGoods;
import com.bytechainx.psi.sale.service.SaleOrderService;
import com.bytechainx.psi.sale.service.StatStoreSaleService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Page;


/**
* 门店统计
*/
@Path("/sale/stat/storeSale")
public class StatStoreSaleController extends BaseController {

	@Inject
	private StatStoreSaleService statStoreSaleService;
	@Inject
	private SaleOrderService saleOrderService;
	
	/**
	* 首页
	*/
	@Permission(Permissions.sale_stat_storeSale)
	public void index() {
	}
	

	/**
	* 按客户列表
	*/
	@Permission(Permissions.sale_stat_storeSale)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<SaleOrder> page = statStoreSaleService.paginateByStore(startTime, endTime, pageNumber, pageSize);
		SaleOrder sumOrder = statStoreSaleService.sumByStore(startTime, endTime, pageNumber, pageSize);
		setAttr("page", page);
		setAttr("sumOrder", sumOrder);
	}
	
	/**
	* 查看客户销售单据详情
	*/
	@Permission(Permissions.sale_stat_storeSale_show)
	public void show() {
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<SaleOrder> sumOrder = statStoreSaleService.paginateByStore(startTime, endTime, 1, pageSize);
		if(sumOrder.getTotalRow() > 0) {
			setAttr("sumOrder", sumOrder.getList().get(0));
		}
		
		setAttr("startTime", startTime);
		setAttr("endTime", endTime);
	}
	
	/**
	* 查看客户销售单据详情
	*/
	@Permission(Permissions.sale_stat_storeSale_show)
	public void showList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer storeId = getInt("tenant_store_id");
		Kv condKv = Kv.create();
		condKv.set("tenant_store_id", storeId);
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
	@Permission(Permissions.sale_stat_storeSale_show)
	public void showGoods() {
		show();
	}
	
	/**
	* 查看客户销售商品详情
	*/
	@Permission(Permissions.sale_stat_storeSale_show)
	public void showGoodsList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<SaleOrderGoods> page = statStoreSaleService.paginateByStoreGoods(startTime, endTime, pageNumber, pageSize);
		
		setAttr("page", page);
	}
	
}