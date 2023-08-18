package com.bytechainx.psi.common.model;

import java.math.BigDecimal;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.StockWarnTypeEnum;
import com.bytechainx.psi.common.model.base.BaseInventoryStock;
import com.jfinal.plugin.activerecord.Db;

/**
 * 商品库存
 */
@SuppressWarnings("serial")
public class InventoryStock extends BaseInventoryStock<InventoryStock> {
	
	public static final InventoryStock dao = new InventoryStock().dao();
	
	/**
	 * 获取商品
	 * @return
	 */
	public GoodsInfo getGoodsInfo() {
		return GoodsInfo.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "goods.info.id."+getGoodsInfoId(), "select * from goods_info where id = ?", getGoodsInfoId());
	}
	
	public GoodsUnit getUnit() {
		return GoodsUnit.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "goods.unit.id."+getUnitId(), "select * from goods_unit where id = ?", getUnitId());
	}
	
	public GoodsSpec getGoodsSpec1() {
		return GoodsSpec.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "goods.spec.id."+getSpec1Id(), "select * from goods_spec where id = ?", getSpec1Id());
	}
	public GoodsSpecOptions getGoodsSpecOption1() {
		return GoodsSpecOptions.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "goods.spec.id."+getSpecOption1Id(), "select * from goods_spec_options where id = ?", getSpecOption1Id());
	}
	public GoodsSpec getGoodsSpec2() {
		return GoodsSpec.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "goods.spec.id."+getSpec2Id(), "select * from goods_spec where id = ?", getSpec2Id());
	}
	public GoodsSpecOptions getGoodsSpecOption2() {
		return GoodsSpecOptions.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "goods.spec.id."+getSpecOption2Id(), "select * from goods_spec_options where id = ?", getSpecOption2Id());
	}
	public GoodsSpec getGoodsSpec3() {
		return GoodsSpec.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "goods.spec.id."+getSpec3Id(), "select * from goods_spec where id = ?", getSpec3Id());
	}
	public GoodsSpecOptions getGoodsSpecOption3() {
		return GoodsSpecOptions.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "goods.spec.id."+getSpecOption3Id(), "select * from goods_spec_options where id = ?", getSpecOption3Id());
	}
	
	/**
	 * 所有规格值
	 * 使用新方法：{@link getGoodsSpecNames()}
	 * @return
	 */
	@Deprecated
	public String getSpecs() {
		StringBuffer sb = new StringBuffer();
		GoodsSpecOptions goodsSpecOption1 = getGoodsSpecOption1();
		if(goodsSpecOption1 != null) {
			sb.append(goodsSpecOption1.getOptionValue() + "/");
		}
		GoodsSpecOptions goodsSpecOption2 = getGoodsSpecOption2();
		if(goodsSpecOption2 != null) {
			sb.append(goodsSpecOption2.getOptionValue() + "/");
		}
		GoodsSpecOptions goodsSpecOption3 = getGoodsSpecOption3();
		if(goodsSpecOption3 != null) {
			sb.append(goodsSpecOption3.getOptionValue() + "/");
		}
		return sb.length() <= 0 ? "" : sb.substring(0, sb.length()-1);
	}
	
	public String getGoodsSpecIds() {
		StringBuffer sb = new StringBuffer();
		if(getSpec1Id() != null && getSpec1Id() > 0) {
			sb.append(getSpec1Id()+":"+getSpecOption1Id());
		}
		if(getSpec2Id() != null && getSpec2Id() > 0) {
			sb.append("|");
			sb.append(getSpec2Id()+":"+getSpecOption2Id());
		}
		if(getSpec3Id() != null && getSpec3Id() > 0) {
			sb.append("|");
			sb.append(getSpec3Id()+":"+getSpecOption3Id());
		}
		return sb.toString();
	}
	
	public String getGoodsSpecNames() {
		GoodsInfo goodsInfo = getGoodsInfo();
		if(!goodsInfo.getSpecFlag()) { // 单规格
			return goodsInfo.getSpecName();
		}
		StringBuffer sb = new StringBuffer();
		if(getSpec1Id() != null && getSpec1Id() > 0) {
			sb.append(getGoodsSpecOption1().getOptionValue());
		}
		if(getSpec2Id() != null && getSpec2Id() > 0) {
			sb.append("/");
			sb.append(getGoodsSpecOption2().getOptionValue());
		}
		if(getSpec3Id() != null && getSpec3Id() > 0) {
			sb.append("/");
			sb.append(getGoodsSpecOption3().getOptionValue());
		}
		return sb.toString();
	}
	
	public GoodsPrice getGoodsPrice() {
		GoodsPrice goodsPrice = GoodsPrice.dao.findBySpec(getGoodsInfoId(), getSpec1Id(), getSpecOption1Id(), getSpec2Id(), getSpecOption2Id(), getSpec3Id(), getSpecOption3Id(), getUnitId());
		if(goodsPrice != null) {
			goodsPrice.put("salePriceMap", JSONObject.parse(goodsPrice.getSalePrice()));
		}
		return goodsPrice;
	}
	
	public GoodsPrice getGoodsPriceByGoodsId() {
		return GoodsPrice.dao.findFirst("select count(*) as counts, ifnull(sum(avg_cost_price),0) as sum_avg_cost_price  from goods_price where goods_info_id = ? group by goods_info_id", getGoodsInfoId());
	}
	
	/**
	 * 页面访问字段，会调用这个方法
	 * @return
	 */
	public BigDecimal getReserve_stock() {
		return getReserveStock();
	}
	
	
	/**
	 * 可用库存
	 * @return
	 */
	public BigDecimal getAvailableStock() {
		return getStock().subtract(getLockStock() == null ? BigDecimal.ZERO : getLockStock());
	}
	
	/**
	 * 查询某个商品单品的库存，包含多个仓库
	 * @return
	 */
	public List<InventoryStock> findBySpec(Integer goodsInfoId, Integer spec1Id, Integer specOption1I, Integer spec2Id, Integer specOption2Id, Integer spec3Id, Integer specOption3Id, Integer unitId) {
		return InventoryStock.dao.find("select * from inventory_stock where goods_info_id = ? and spec_1_id = ? and spec_option_1_id = ? and spec_2_id = ? and spec_option_2_id = ? and spec_3_id = ? and spec_option_3_id = ? and unit_id = ?", goodsInfoId, spec1Id, specOption1I, spec2Id, specOption2Id, spec3Id, specOption3Id, unitId);
	}
	
	/**
	 * 查询某个商品单品某个仓库的库存
	 * @return
	 */
	public InventoryStock findByWarehouse(Integer goodsInfoId, Integer spec1Id, Integer specOption1Id, Integer spec2Id, Integer specOption2Id, Integer spec3Id, Integer specOption3Id, Integer unitId) {
		InventoryStock inventoryStock = InventoryStock.dao.findFirst("select * from inventory_stock where goods_info_id = ? and spec_1_id = ? and spec_option_1_id = ? and spec_2_id = ? and spec_option_2_id = ? and spec_3_id = ? and spec_option_3_id = ? and unit_id = ? limit 1", goodsInfoId, spec1Id, specOption1Id, spec2Id, specOption2Id, spec3Id, specOption3Id, unitId);
		return inventoryStock;
	}
	public InventoryStock findSumStock(Integer goodsInfoId, Integer spec1Id, Integer specOption1I, Integer spec2Id, Integer specOption2Id, Integer spec3Id, Integer specOption3Id, Integer unitId) {
		return InventoryStock.dao.findFirst("select ifnull(sum(stock),0) as stock from inventory_stock where goods_info_id = ? and spec_1_id = ? and spec_option_1_id = ? and spec_2_id = ? and spec_option_2_id = ? and spec_3_id = ? and spec_option_3_id = ? and unit_id = ? limit 1", goodsInfoId, spec1Id, specOption1I, spec2Id, specOption2Id, spec3Id, specOption3Id, unitId);
	}
	
	
	/**
	 * 获取商品库存预警
	 */
	public boolean isWarning() {
		Long counts = Db.queryLong("select count(*) as counts from inventory_stock where goods_info_id = ? and warn_type != ? ", getGoodsInfoId(), StockWarnTypeEnum.ok.getValue());
		if(counts > 0) {
			return true;
		}
		return false;
	}
}

