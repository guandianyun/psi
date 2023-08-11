package com.bytechainx.psi.web.web.controller.inventory;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.StockWarnTypeEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.model.GoodsCategory;
import com.bytechainx.psi.common.model.GoodsInfo;
import com.bytechainx.psi.common.model.GoodsUnit;
import com.bytechainx.psi.common.model.InventoryStock;
import com.bytechainx.psi.common.model.InventoryStockLog;
import com.bytechainx.psi.common.service.goods.GoodsInfoService;
import com.bytechainx.psi.purchase.service.StatStockService;
import com.bytechainx.psi.purchase.service.StockInfoService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 库存查询
*/
@Path("/inventory/stock/info")
public class StockInfoController extends BaseController {

	@Inject
	private StockInfoService stockInfoService;
	@Inject
	private GoodsInfoService goodsInfoService;
	@Inject
	private StatStockService statStockService;

	/**
	* 首页
	*/
	@Permission(Permissions.inventory_stock_info)
	public void index() {
		List<GoodsCategory> topCategoryList = GoodsCategory.dao.findTop();
		setAttr("topCategoryList", topCategoryList);
	}

	/**
	* 列表
	*/
	@Permission(Permissions.inventory_stock_info)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		
		Integer supplierId = getInt("supplier_info_id");
		Integer goodsCategoryId = getInt("goods_category_id");
		Boolean hideStopFlag = getBoolean("hide_stop_flag"); // 隐藏停用商品
		Boolean hideZeroFlag = getBoolean("hide_zero_flag", false); // 隐藏零库存
		Boolean hideWarnFlag = getBoolean("hide_warn_flag", false); // 只显示预警商品
		String keyword = get("keyword");

		List<Integer> goodsInfoIds = new ArrayList<>();
		if((supplierId != null && supplierId > 0) || (goodsCategoryId != null && goodsCategoryId >= 0) || StringUtils.isNotEmpty(keyword)) {
			Kv goodsCondKv = Kv.create();
			goodsCondKv.set("supplier_info_id", supplierId);
			goodsCondKv.set("goods_category_id", goodsCategoryId);
			goodsCondKv.set("name,code,bar_code", keyword); // 多字段模糊查询
			Page<GoodsInfo> goodsPage = goodsInfoService.paginate(goodsCondKv, 1, 1000);
			for (GoodsInfo goodsInfo : goodsPage.getList()) {
				goodsInfoIds.add(goodsInfo.getId());
			}
		}
		Kv condKv = Kv.create();
		if(hideStopFlag) {
			condKv.set("data_status", DataStatusEnum.enable.getValue());
		} else {
			ConditionFilter filter = new ConditionFilter();
			filter.setOperator(Operator.neq);
			filter.setValue(DataStatusEnum.delete.getValue());
			condKv.set("data_status", filter);
		}
		if(hideZeroFlag) {
			ConditionFilter filter = new ConditionFilter();
			filter.setOperator(Operator.neq);
			filter.setValue(0);
			condKv.set("stock", filter);
		}
		if(hideWarnFlag) {
			ConditionFilter filter = new ConditionFilter();
			filter.setOperator(Operator.neq);
			filter.setValue(StockWarnTypeEnum.ok.getValue());
			condKv.set("warn_type", filter);
		}
		
		// FIXME 这里以后要优化下查询方式，要不然商品很多的话，in()SQL会很长
		if(!goodsInfoIds.isEmpty() || StringUtils.isNotEmpty(keyword)) {
			ConditionFilter filter = new ConditionFilter();
			filter.setOperator(Operator.in);
			if(goodsInfoIds.isEmpty()) {
				filter.setValue("0");
			} else {
				filter.setValue(StringUtils.join(goodsInfoIds, ","));
			}
			condKv.set("goods_info_id", filter);
		}
		
		Page<InventoryStock> page = stockInfoService.paginate(condKv, pageNumber, pageSize);
		setAttr("page", page);
		setAttr("hideStopFlag", hideStopFlag);
		setAttr("hideZeroFlag", hideZeroFlag);
		setAttr("hideWarnFlag", hideWarnFlag);

	}

	/**
	* 查看
	*/
	@Permission(Permissions.inventory_stock_info_show)
	public void show() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		GoodsInfo info = GoodsInfo.dao.findById(id);
		if(info == null) {
			renderError(404);
			return;
		}
		GoodsUnit stockUnit = info.getStockMainUnit();
		setAttr("stockUnit", stockUnit);
		setAttr("goodsInfo", info);
	}
	
	/**
	* 库存流水明细
	*/
	@Permission(Permissions.inventory_stock_info_show)
	public void showLog() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		GoodsInfo info = GoodsInfo.dao.findById(id);
		if(info == null) {
			renderError(404);
			return;
		}
		setAttr("goodsInfo", info);
	}
	
	@Permission(Permissions.inventory_stock_info_show)
	public void showLogList() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		
		Kv condKv = Kv.create();
		condKv.set("goods_info_id", id);
		
		Page<InventoryStockLog>  page = statStockService.paginate(condKv, pageNumber, pageSize);
		setAttr("page", page);
	}
	
	/**
	 * 查询库存
	 */
	@Permission({Permissions.inventory, Permissions.sale_sale})
	public void showByJson() {
		Integer goodsInfoId = getInt("goods_info_id");
		String specstring  = get("goods_spec_ids");
		
		Integer spec1Id = 0;
		Integer specOption1Id = 0;
		Integer spec2Id = 0;
		Integer specOption2Id = 0;
		Integer spec3Id = 0; 
		Integer specOption3Id = 0;
		
		String[] specList = StringUtils.split(specstring, "|");
		if(specList != null && specList.length > 0) {
			String[] spec = StringUtils.split(specList[0], ":"); 
			spec1Id = Integer.parseInt((spec[0]));
			specOption1Id = Integer.parseInt((spec[1]));
		}
		if(specList != null && specList.length > 1) {
			String[] spec = StringUtils.split(specList[1], ":"); 
			spec2Id = Integer.parseInt((spec[0]));
			specOption2Id = Integer.parseInt((spec[1]));
		}
		if(specList != null && specList.length > 2) {
			String[] spec = StringUtils.split(specList[2], ":"); 
			spec3Id = Integer.parseInt((spec[0]));
			specOption3Id = Integer.parseInt((spec[1]));
		}
		GoodsInfo goodsInfo = GoodsInfo.dao.findById(goodsInfoId);
		GoodsUnit stockUnit = goodsInfo.getStockMainUnit(); // 通过库存单位查询库存
		
		InventoryStock inventoryStock = InventoryStock.dao.findByWarehouse(goodsInfoId, spec1Id, specOption1Id, spec2Id, specOption2Id, spec3Id, specOption3Id, stockUnit.getId());
		if(inventoryStock == null) {
			inventoryStock = new InventoryStock();
			inventoryStock.setStock(BigDecimal.ZERO);
			inventoryStock.setReserveStock(BigDecimal.ZERO);
			inventoryStock.setLockStock(BigDecimal.ZERO);
			inventoryStock.setGoodsInfoId(goodsInfoId);
			inventoryStock.setSpec1Id(spec1Id);
			inventoryStock.setSpecOption1Id(specOption1Id);
			inventoryStock.setSpec2Id(spec2Id);
			inventoryStock.setSpecOption2Id(specOption2Id);
			inventoryStock.setSpec3Id(spec3Id);
			inventoryStock.setSpecOption3Id(specOption3Id);
			inventoryStock.setUnitId(stockUnit.getId());
			
		}
		renderJson(Ret.ok().set("data", inventoryStock));
	}
}