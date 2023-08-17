package com.bytechainx.psi.web.web.controller.inventory;


import java.math.BigDecimal;
import java.util.Arrays;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.PurchaseOrder;
import com.bytechainx.psi.common.model.SupplierCategory;
import com.bytechainx.psi.common.model.SupplierInfo;
import com.bytechainx.psi.purchase.service.PurchaseOrderService;
import com.bytechainx.psi.purchase.service.SupplierCategoryService;
import com.bytechainx.psi.purchase.service.SupplierInfoService;
import com.bytechainx.psi.web.epc.TraderEventProducer;
import com.bytechainx.psi.web.epc.event.inventory.SupplierInfoEvent;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 供应商管理
*/
@Path("/inventory/supplier/info")
public class SupplierInfoController extends BaseController {


	@Inject
	private SupplierInfoService supplierInfoService;
	@Inject
	private SupplierCategoryService supplierCategoryService;
	@Inject
	private PurchaseOrderService purchaseOrderService;
	@Inject
	private TraderEventProducer traderEventProducer;

	/**
	* 首页
	*/
	@Permission(Permissions.inventory_supplier_info)
	public void index() {
		setAttrCommon();
	}

	/**
	* 列表
	*/
	@Permission(Permissions.inventory_supplier_info)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Integer supplierCategoryId = getInt("supplier_category_id");
		Boolean hideStopFlag = getBoolean("hide_stop_flag"); // 隐藏停用客户
		Boolean hideDebtFlag = getBoolean("hide_debt_flag", false); // 隐藏无欠款客户
		String keyword = get("keyword");
		Kv condKv = Kv.create();
		condKv.set("supplier_category_id", supplierCategoryId);
		condKv.set("name,contact,mobile,remark", keyword); // 多字段模糊查询
		
		if(hideStopFlag) {
			condKv.set("data_status", DataStatusEnum.enable.getValue());
		}
		Kv moreCondKv = Kv.create();
		moreCondKv.set("hide_debt_flag", hideDebtFlag);
		
		Page<SupplierInfo> page = supplierInfoService.paginate(condKv, moreCondKv, pageNumber, pageSize);
		setAttr("page", page);
		setAttr("hideStopFlag", hideStopFlag);
		setAttr("hideDebtFlag", hideDebtFlag);
	}
	

	/**
	 * 查询供应商
	 */
	@Permission({Permissions.inventory_supplier, Permissions.goods_goods, Permissions.inventory_purchase})
	public void listByJson() {
		int pageNumber = getInt("pageNumber", 1);
		String keyword = get("keyword");
		Kv condKv = Kv.create();
		condKv.set("data_status", DataStatusEnum.enable.getValue());
		condKv.set("name,code", keyword); // 多字段模糊查询
		Page<SupplierInfo> page = supplierInfoService.paginate(condKv, null, pageNumber, maxPageSize);
		for (SupplierInfo supplier : page.getList()) {
			supplier.put("debtAmount", supplier.getDebtAmount());
			supplier.put("availableAmount", supplier.getAvailableAmount());
		}
		renderJson(Ret.ok().set("data", page.getList()));
	}

	/**
	* 查看
	*/
	@Permission(Permissions.inventory_supplier_info_show)
	public void show() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		SupplierInfo info = SupplierInfo.dao.findById(id);
		if(info == null) {
			renderError(404);
			return;
		}
		Kv purchaseOrderCondKv = Kv.create();
		purchaseOrderCondKv.set("supplier_info_id", id);
		purchaseOrderCondKv.set("order_status", OrderStatusEnum.normal.getValue());
		Page<PurchaseOrder> purchaseOrderPage = purchaseOrderService.paginate(null, null, purchaseOrderCondKv, 1, 10);
		
		setAttr("purchaseOrderPage", purchaseOrderPage);
		setAttr("supplierInfo", info);
	}


	/**
	* 添加
	*/
	@Permission(Permissions.inventory_supplier_info_create)
	public void add() {
		setAttrCommon();
	}

	/**
	* 新增
	*/
	@Permission(Permissions.inventory_supplier_info_create)
	public void create() {
		SupplierInfo info = getModel(SupplierInfo.class, "", true);
		BigDecimal openBalance = get("open_balance") == null ? null : new BigDecimal(get("open_balance") ); // 期初欠款
		if(openBalance != null) {
			info.put("open_balance", openBalance.multiply(new BigDecimal(getInt("amount_type"))));// 欠款为正数，余额为负数
		}
		Ret ret = traderEventProducer.request(getAdminId(), new SupplierInfoEvent("create"), info);
		renderJson(ret);
	}



	/**
	* 编辑
	*/
	@Permission(Permissions.inventory_supplier_info_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		SupplierInfo info = SupplierInfo.dao.findById(id);
		if(info == null) {
			renderError(404);
			return;
		}
		setAttr("supplierInfo", info);
		setAttrCommon();
		
	}

	/**
	* 修改
	*/
	@Permission(Permissions.inventory_supplier_info_update)
	public void update() {
		SupplierInfo info = getModel(SupplierInfo.class, "", true);
		BigDecimal openBalance = get("open_balance") == null ? null : new BigDecimal(get("open_balance") ); // 期初欠款
		if(openBalance != null) {
			info.put("open_balance", openBalance.multiply(new BigDecimal(getInt("amount_type"))));// 欠款为正数，余额为负数
		}
		Ret ret = traderEventProducer.request(getAdminId(), new SupplierInfoEvent("update"), info);
		renderJson(ret);
	}



	/**
	* 删除
	*/
	@Permission(Permissions.inventory_supplier_info_delete)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = supplierInfoService.delete(Arrays.asList(id));
		renderJson(ret);
	}



	/**
	* 停用
	*/
	@Permission(Permissions.inventory_supplier_info_disable)
	@Before(Tx.class)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = supplierInfoService.disable(Arrays.asList(id));

		renderJson(ret);
	}
	
	/**
	* 启用
	*/
	@Permission(Permissions.inventory_supplier_info_disable)
	@Before(Tx.class)
	public void enable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = supplierInfoService.enable(Arrays.asList(id));

		renderJson(ret);
	}



	/**
	* 导入
	*/
	@Permission(Permissions.inventory_supplier_info_createImport)
	public void createImport() {

		renderJson(Ret.ok());
	}
	
	/**
	 * 设置公共数据
	 */
	private void setAttrCommon() {
		Kv priceLevelCondKv = Kv.create();
		priceLevelCondKv.set("data_status", DataStatusEnum.enable.getValue());
		
		Page<SupplierCategory> supplierCategoryPage = supplierCategoryService.paginate(1, maxPageSize);
		
		setAttr("supplierCategoryPage", supplierCategoryPage);
	}

}