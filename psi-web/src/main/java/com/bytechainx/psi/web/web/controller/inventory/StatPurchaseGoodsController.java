package com.bytechainx.psi.web.web.controller.inventory;


import java.util.List;

import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.GoodsCategory;
import com.bytechainx.psi.common.model.GoodsInfo;
import com.bytechainx.psi.common.model.PurchaseOrderGoods;
import com.bytechainx.psi.purchase.service.StatPurchaseGoodsService;
import com.bytechainx.psi.purchase.service.StatPurchaseService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.plugin.activerecord.Page;


/**
* 商品统计
*/
@Path("/inventory/stat/purchaseGoods")
public class StatPurchaseGoodsController extends BaseController {

	@Inject
	private StatPurchaseGoodsService purchaseGoodsService;
	@Inject
	private StatPurchaseService statPurchaseService;
	
	/**
	* 首页
	*/
	@Permission(Permissions.inventory_stat_purchaseGoods)
	public void index() {
		List<GoodsCategory> topCategoryList = GoodsCategory.dao.findTop();
		setAttr("topCategoryList", topCategoryList);
	}
	
	/**
	* 按产品列表
	*/
	@Permission(Permissions.inventory_stat_purchaseGoods)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer goodsCategoryId = getInt("goods_category_id");
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<PurchaseOrderGoods> page = purchaseGoodsService.paginate(goodsCategoryId, startTime, endTime, pageNumber, pageSize);
		PurchaseOrderGoods sumOrder = purchaseGoodsService.sumByCategory(goodsCategoryId, startTime, endTime);
		setAttr("page", page);
		setAttr("sumOrder", sumOrder);
	}
	
	/**
	* 查看详情
	*/
	@Permission(Permissions.inventory_stat_purchaseGoods_show)
	public void show() {
		Integer goodsInfoId = getInt("goods_info_id");
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		GoodsInfo goodsInfo = GoodsInfo.dao.findById(goodsInfoId);
		
		PurchaseOrderGoods sumOrder = purchaseGoodsService.sumByGoodsId(goodsInfoId, startTime, endTime);
		setAttr("sumOrder", sumOrder);
		
		setAttr("goodsInfo", goodsInfo);
		setAttr("startTime", startTime);
		setAttr("endTime", endTime);
	}
	
	/**
	* 查看单据列表
	*/
	@Permission(Permissions.inventory_stat_purchaseGoods_show)
	public void showList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer goodsInfoId = getInt("goods_info_id");
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<PurchaseOrderGoods> page = purchaseGoodsService.paginateByGoods(goodsInfoId, startTime, endTime, pageNumber, pageSize);
		
		setAttr("page", page);
	}
	
	/**
	* 查看详情
	*/
	@Permission(Permissions.inventory_stat_purchaseGoods_show)
	public void showSupplier() {
		show();
	}
	
	/**
	* 查看单据列表
	*/
	@Permission(Permissions.inventory_stat_purchaseGoods_show)
	public void showSupplierList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer goodsInfoId = getInt("goods_info_id");
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<PurchaseOrderGoods> page = purchaseGoodsService.paginateBySupplier(goodsInfoId, startTime, endTime, pageNumber, pageSize);
		
		setAttr("page", page);
	}
}