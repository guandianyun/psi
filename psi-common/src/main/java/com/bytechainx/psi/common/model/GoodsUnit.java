package com.bytechainx.psi.common.model;

import java.util.List;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.model.base.BaseGoodsUnit;

/**
 * 商品规格
 */
@SuppressWarnings("serial")
public class GoodsUnit extends BaseGoodsUnit<GoodsUnit> {
	
	public static final GoodsUnit dao = new GoodsUnit().dao();
	
	public GoodsUnit findById(Integer id) {
		return GoodsUnit.dao.findFirst("select * from goods_unit where id = ? and data_status != ? limit 1", id, DataStatusEnum.delete.getValue());
	}

	public List<GoodsUnit> findAll() {
		return GoodsUnit.dao.find("select * from goods_unit where data_status = ?", DataStatusEnum.enable.getValue());
	}
	
}

