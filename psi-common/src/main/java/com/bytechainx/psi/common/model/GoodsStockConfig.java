package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.model.base.BaseGoodsStockConfig;

/**
 * 商品库存预警设置
 */
@SuppressWarnings("serial")
public class GoodsStockConfig extends BaseGoodsStockConfig<GoodsStockConfig> {
	
	public static final GoodsStockConfig dao = new GoodsStockConfig().dao();
	
	public GoodsStockConfig findBySpec(Integer goodsInfoId, Integer spec1Id, Integer specOption1I, Integer spec2Id, Integer specOption2Id, Integer spec3Id, Integer specOption3Id, Integer unitId) {
		return GoodsStockConfig.dao.findFirst("select * from goods_stock_config where goods_info_id = ? and spec_1_id = ? and spec_option_1_id = ? and spec_2_id = ? and spec_option_2_id = ? and spec_3_id = ? and spec_option_3_id = ? and unit_id = ? and inventory_warehouse_id = ? limit 1", goodsInfoId, spec1Id, specOption1I, spec2Id, specOption2Id, spec3Id, specOption3Id, unitId);
	}
	
}

