package com.bytechainx.psi.web.web.controller.inventory;


import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.PurchaseOrder;
import com.bytechainx.psi.common.model.PurchaseOrderGoods;
import com.bytechainx.psi.common.model.SupplierCategory;
import com.bytechainx.psi.common.model.SupplierInfo;
import com.bytechainx.psi.purchase.service.PurchaseOrderService;
import com.bytechainx.psi.purchase.service.StatPurchaseService;
import com.bytechainx.psi.purchase.service.SupplierCategoryService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Page;


/**
* 进货统计
*/
@Path("/inventory/stat/purchase")
public class StatPurchaseController extends BaseController {

	@Inject
	private StatPurchaseService statPurchaseService;
	@Inject
	private PurchaseOrderService purchaseOrderService;
	@Inject
	private SupplierCategoryService supplierCategoryService;
	
	/**
	* 首页
	*/
	@Permission(Permissions.inventory_stat_purchase)
	public void index() {
		Page<SupplierCategory> supplierCategoryPage = supplierCategoryService.paginate(1, maxPageSize);
		setAttr("supplierCategoryPage", supplierCategoryPage);
	}

	/**
	* 按客户列表
	*/
	@Permission(Permissions.inventory_stat_purchase)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer supplierInfoId = getInt("supplier_info_id");
		Integer supplierCategoryId = getInt("supplier_category_id");
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<PurchaseOrder> page = statPurchaseService.paginateBySupplier(supplierCategoryId, supplierInfoId, startTime, endTime, pageNumber, pageSize);
		PurchaseOrder sumOrder = statPurchaseService.sumBySupplier(supplierCategoryId, supplierInfoId, startTime, endTime);
		setAttr("page", page);
		setAttr("sumOrder", sumOrder);
	}
	
	/**
	* 查看客户销售单据详情
	*/
	@Permission(Permissions.inventory_stat_purchase_show)
	public void show() {
		Integer supplierInfoId = getInt("supplier_info_id");
		Integer supplierCategoryId = getInt("supplier_category_id");
		if(supplierInfoId == null || supplierInfoId <= 0) {
			renderError(404);
			return;
		}
		SupplierInfo supplierInfo = SupplierInfo.dao.findById(supplierInfoId);
		if(supplierInfo == null) {
			renderError(404);
			return;
		}
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<PurchaseOrder> sumOrder = statPurchaseService.paginateBySupplier(supplierCategoryId, supplierInfoId, startTime, endTime, 1, pageSize);
		if(sumOrder.getTotalRow() > 0) {
			setAttr("sumOrder", sumOrder.getList().get(0));
		}
		
		setAttr("supplierInfo", supplierInfo);
		setAttr("startTime", startTime);
		setAttr("endTime", endTime);
	}
	
	/**
	* 查看客户销售单据详情
	*/
	@Permission(Permissions.inventory_stat_purchase_show)
	public void showList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer supplierInfoId = getInt("supplier_info_id");
		Integer tenantStoreId = getInt("tenant_store_id");
		Kv condKv = Kv.create();
		condKv.set("tenant_store_id", tenantStoreId);
		condKv.set("supplier_info_id", supplierInfoId);
		condKv.set("order_status", OrderStatusEnum.normal.getValue());
		condKv.set("audit_status", AuditStatusEnum.pass.getValue());
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<PurchaseOrder> page = purchaseOrderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		
		setAttr("page", page);
	}
	
	/**
	* 查看客户销售商品详情
	*/
	@Permission(Permissions.inventory_stat_purchase_show)
	public void showGoods() {
		show();
	}
	
	/**
	* 查看客户销售商品详情
	*/
	@Permission(Permissions.inventory_stat_purchase_show)
	public void showGoodsList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer supplierInfoId = getInt("supplier_info_id");
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<PurchaseOrderGoods> page = statPurchaseService.paginateBySupplierGoods(supplierInfoId, startTime, endTime, pageNumber, pageSize);
		
		setAttr("page", page);
	}
}