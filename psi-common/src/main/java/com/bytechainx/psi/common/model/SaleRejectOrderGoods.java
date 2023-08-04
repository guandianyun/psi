package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.EnumConstant.RejectDealTypeEnum;
import com.bytechainx.psi.common.model.base.BaseSaleRejectOrderGoods;

/**
 * 退货单关联商品
 */
@SuppressWarnings("serial")
public class SaleRejectOrderGoods extends BaseSaleRejectOrderGoods<SaleRejectOrderGoods> {
	
	public static final SaleRejectOrderGoods dao = new SaleRejectOrderGoods().dao();
	
	/**
	 * 查询进货单对应的某个商品
	 * @return
	 */
	public SaleRejectOrderGoods findBySpec(Integer orderId, Integer goodsInfoId, Integer spec1Id, Integer specOption1I, Integer spec2Id, Integer specOption2Id, Integer spec3Id, Integer specOption3Id, Integer unitId) {
		return SaleRejectOrderGoods.dao.findFirst("select * from sale_reject_order_goods where sale_reject_order_id = ? and goods_info_id = ? and spec_1_id = ? and spec_option_1_id = ? and spec_2_id = ? and spec_option_2_id = ? and spec_3_id = ? and spec_option_3_id = ? and unit_id = ? limit 1", orderId, goodsInfoId, spec1Id, specOption1I, spec2Id, specOption2Id, spec3Id, specOption3Id, unitId);
	}
	
	public GoodsInfo getGoodsInfo() {
		return GoodsInfo.dao.findById(getGoodsInfoId());
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
			sb.append(getSpecOption1().getOptionValue());
		}
		if(getSpec2Id() != null && getSpec2Id() > 0) {
			sb.append("/");
			sb.append(getSpecOption2().getOptionValue());
		}
		if(getSpec3Id() != null && getSpec3Id() > 0) {
			sb.append("/");
			sb.append(getSpecOption3().getOptionValue());
		}
		return sb.toString();
	}
	
	
	public GoodsUnit getGoodsUnit() {
		return GoodsUnit.dao.findById(getUnitId());
	}
	public GoodsSpec getGoodsSpec1() {
		return GoodsSpec.dao.findById(getSpec1Id());
	}
	public GoodsSpec getGoodsSpec2() {
		return GoodsSpec.dao.findById(getSpec2Id());
	}
	public GoodsSpec getGoodsSpec3() {
		return GoodsSpec.dao.findById(getSpec3Id());
	}
	public GoodsSpecOptions getSpecOption1() {
		return GoodsSpecOptions.dao.findById(getSpecOption1Id());
	}
	public GoodsSpecOptions getSpecOption2() {
		return GoodsSpecOptions.dao.findById(getSpecOption2Id());
	}
	public GoodsSpecOptions getSpecOption3() {
		return GoodsSpecOptions.dao.findById(getSpecOption3Id());
	}

	public SaleRejectOrderGoods findById(Integer id) {
		return SaleRejectOrderGoods.dao.findFirst("select * from sale_reject_order_goods where id = ? limit 1", id);
	}
	
	public String getRejectDealName() {
		if(getRejectDealType() != null) {
			return RejectDealTypeEnum.getEnum(getRejectDealType()).getName();
		}
		return "";
	}
	
}

