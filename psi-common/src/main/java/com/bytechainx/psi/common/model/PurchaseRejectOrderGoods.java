package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.model.base.BasePurchaseRejectOrderGoods;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class PurchaseRejectOrderGoods extends BasePurchaseRejectOrderGoods<PurchaseRejectOrderGoods> {
	
	public static final PurchaseRejectOrderGoods dao = new PurchaseRejectOrderGoods().dao();
	
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

	public PurchaseRejectOrderGoods findById(Integer id) {
		return PurchaseRejectOrderGoods.dao.findFirst("select * from purchase_reject_order_goods where id = ? limit 1", id);
	}
}
