package com.bytechainx.psi.web.web.controller.sale;


import java.util.List;

import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.GoodsCategory;
import com.bytechainx.psi.common.model.GoodsInfo;
import com.bytechainx.psi.common.model.SaleOrderGoods;
import com.bytechainx.psi.sale.service.StatHotSaleService;
import com.bytechainx.psi.sale.service.StatSaleService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.plugin.activerecord.Page;


/**
* 热销分析
*/
@Path("/sale/stat/hotSale")
public class StatHotSaleController extends BaseController {

	@Inject
	private StatHotSaleService hotSaleService;
	@Inject
	private StatSaleService statSaleService;
	
	/**
	* 首页
	*/
	@Permission(Permissions.sale_stat_hotSale)
	public void index() {
		List<GoodsCategory> topCategoryList = GoodsCategory.dao.findTop();
		setAttr("topCategoryList", topCategoryList);
	}
	
	/**
	* 按产品列表
	*/
	@Permission(Permissions.sale_stat_hotSale)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer goodsCategoryId = getInt("goods_category_id");
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<SaleOrderGoods> page = hotSaleService.paginate(goodsCategoryId, startTime, endTime, pageNumber, pageSize);
		SaleOrderGoods sumOrder = hotSaleService.sumByCategory(goodsCategoryId, startTime, endTime);
		setAttr("page", page);
		setAttr("sumOrder", sumOrder);
	}
	
	/**
	* 查看详情
	*/
	@Permission(Permissions.sale_stat_hotSale_show)
	public void show() {
		Integer goodsInfoId = getInt("goods_info_id");
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		GoodsInfo goodsInfo = GoodsInfo.dao.findById(goodsInfoId);
		
		SaleOrderGoods sumOrder = hotSaleService.sumByGoodsId(goodsInfoId, startTime, endTime);
		setAttr("sumOrder", sumOrder);
		
		setAttr("goodsInfo", goodsInfo);
		setAttr("startTime", startTime);
		setAttr("endTime", endTime);
	}
	
	/**
	* 查看单据列表
	*/
	@Permission(Permissions.sale_stat_hotSale_show)
	public void showList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer goodsInfoId = getInt("goods_info_id");
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<SaleOrderGoods> page = hotSaleService.paginateByGoods(goodsInfoId, startTime, endTime, pageNumber, pageSize);
		
		setAttr("page", page);
	}
	
	/**
	* 查看详情
	*/
	@Permission(Permissions.sale_stat_hotSale_show)
	public void showCustomer() {
		show();
	}
	
	/**
	* 查看单据列表
	*/
	@Permission(Permissions.sale_stat_hotSale_show)
	public void showCustomerList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer goodsInfoId = getInt("goods_info_id");
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<SaleOrderGoods> page = hotSaleService.paginateByCustomer(goodsInfoId, startTime, endTime, pageNumber, pageSize);
		
		setAttr("page", page);
	}
	
}