package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.model.base.BaseInventoryStockLog;

/**
 * 库存流水
 */
@SuppressWarnings("serial")
public class InventoryStockLog extends BaseInventoryStockLog<InventoryStockLog> {
	
	public static final InventoryStockLog dao = new InventoryStockLog().dao();
	
	/**
	 * 查询商品单品对应的单据库存记录
	 * @return
	 */
	public InventoryStockLog findByOrderId(Integer orderId, Integer orderType, Integer spec1Id, Integer specOption1I, Integer spec2Id, Integer specOption2Id, Integer spec3Id, Integer specOption3Id, Integer unitId, Integer warehouseId) {
		return InventoryStockLog.dao.findFirst("select * from inventory_stock_log where order_id = ? and order_type = ? and spec_1_id = ? and spec_option_1_id = ? and spec_2_id = ? and spec_option_2_id = ? and spec_3_id = ? and spec_option_3_id = ? and unit_id = ? and inventory_warehouse_id = ? limit 1", orderId, orderType, spec1Id, specOption1I, spec2Id, specOption2Id, spec3Id, specOption3Id, unitId);
	}
	
	public GoodsUnit getUnit() {
		return GoodsUnit.dao.findById(getUnitId());
	}
	/**
	 * 获取商品
	 * @return
	 */
	public GoodsInfo getGoodsInfo() {
		return GoodsInfo.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "goods.info.id."+getGoodsInfoId(), "select * from goods_info where id = ?", getGoodsInfoId());
	}
	
	public GoodsSpec getGoodsSpec1() {
		return GoodsSpec.dao.findById(getSpec1Id());
	}
	public GoodsSpecOptions getGoodsSpecOption1() {
		return GoodsSpecOptions.dao.findById(getSpecOption1Id());
	}
	public GoodsSpec getGoodsSpec2() {
		return GoodsSpec.dao.findById(getSpec2Id());
	}
	public GoodsSpecOptions getGoodsSpecOption2() {
		return GoodsSpecOptions.dao.findById(getSpecOption2Id());
	}
	public GoodsSpec getGoodsSpec3() {
		return GoodsSpec.dao.findById(getSpec3Id());
	}
	public GoodsSpecOptions getGoodsSpecOption3() {
		return GoodsSpecOptions.dao.findById(getSpecOption3Id());
	}
	
	/**
	 * 所有规格值
	 * @return
	 */
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
	
}

