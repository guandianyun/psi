package com.bytechainx.psi.common.model;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.model.base.BaseGoodsPrice;

/**
 * 商品价格
 */
@SuppressWarnings("serial")
public class GoodsPrice extends BaseGoodsPrice<GoodsPrice> {
	
	public static final GoodsPrice dao = new GoodsPrice().dao();
	
	/**
	 * 查询价格
	 * @return
	 */
	public GoodsPrice findBySpec(Integer goodsInfoId, Integer spec1Id, Integer specOption1I, Integer spec2Id, Integer specOption2Id, Integer spec3Id, Integer specOption3Id, Integer unitId) {
		return GoodsPrice.dao.findFirst("select * from goods_price where goods_info_id = ? and spec_1_id = ? and spec_option_1_id = ? and spec_2_id = ? and spec_option_2_id = ? and spec_3_id = ? and spec_option_3_id = ? and unit_id = ? limit 1", goodsInfoId, spec1Id, specOption1I, spec2Id, specOption2Id, spec3Id, specOption3Id, unitId);
	}
	/**
	 * 查询商品价格表
	 * @param tenantOrgId
	 * @param goodsInfoId
	 * @return
	 */
	public List<GoodsPrice> findPriceList(Integer goodsInfoId) {
		return GoodsPrice.dao.find("select * from goods_price where goods_info_id = ?", goodsInfoId);
	}
	
	public GoodsInfo getGoodsInfo() {
		return GoodsInfo.dao.findById(getGoodsInfoId());
	}
	
	public GoodsUnit getGoodsUnit() {
		return GoodsUnit.dao.findById(getUnitId());
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
	
	public GoodsSpecOptions getSpecOption1() {
		return GoodsSpecOptions.dao.findById(getSpecOption1Id());
	}
	public GoodsSpecOptions getSpecOption2() {
		return GoodsSpecOptions.dao.findById(getSpecOption2Id());
	}
	public GoodsSpecOptions getSpecOption3() {
		return GoodsSpecOptions.dao.findById(getSpecOption3Id());
	}
	
	/**
	 * 获取客户的销售价
	 * @param customerInfoId
	 * @return
	 */
	public BigDecimal getCustomerSalePrice(Integer customerInfoId) {
		CustomerInfo customerInfo = CustomerInfo.dao.findById(customerInfoId);
		if(customerInfo == null || customerInfo.getCustomerPriceLevelId() == null || customerInfo.getCustomerPriceLevelId() <= 0) {
			return BigDecimal.ZERO;
		}
		CustomerPriceLevel priceLevel = customerInfo.getCustomerPriceLevel(); // 客户的价格等级
		JSONObject salePrice = JSONObject.parseObject(getSalePrice()); // 查找客户的销售价格
		if(salePrice == null) {
			return BigDecimal.ZERO;
		}
		String priceString = salePrice.getString(priceLevel.getId()+"");
		if(StringUtils.isEmpty(priceString)) {
			return BigDecimal.ZERO;
		}
		return new BigDecimal(priceString);
	}
	
	/**
	 * 获取零售价
	 * @return
	 */
	public BigDecimal getRetailSalePrice() {
		JSONObject salePrice = JSONObject.parseObject(getSalePrice()); // 查找客户的销售价格
		if(salePrice == null) {
			return null;
		}
		CustomerPriceLevel priceLevel = CustomerPriceLevel.dao.findRetailPriceLevel(); // 零售价格等级
		String priceString = salePrice.getString(priceLevel.getId()+"");
		if(StringUtils.isEmpty(priceString)) {
			return null;
		}
		return new BigDecimal(priceString);
	}
	
}

