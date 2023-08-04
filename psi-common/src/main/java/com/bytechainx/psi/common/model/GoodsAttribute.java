package com.bytechainx.psi.common.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.GoodsAttrTypeEnum;
import com.bytechainx.psi.common.model.base.BaseGoodsAttribute;

/**
 * 商品属性
 */
@SuppressWarnings("serial")
public class GoodsAttribute extends BaseGoodsAttribute<GoodsAttribute> {
	
	public static final GoodsAttribute dao = new GoodsAttribute().dao();
	
	public GoodsAttribute findById(Integer id) {
		return GoodsAttribute.dao.findFirst("select * from goods_attribute where id = ? and data_status != ? limit 1", id, DataStatusEnum.delete.getValue());
	}

	public List<GoodsAttribute> findAll() {
		return GoodsAttribute.dao.find("select * from goods_attribute ");
	}
	
	public String[] getAttrValueList() {
		return StringUtils.split(getAttrValues(), ",");
	}
	
	public String getAttrTypeName() {
		return GoodsAttrTypeEnum.getEnum(getAttrType()).getName();
	}
	
}

