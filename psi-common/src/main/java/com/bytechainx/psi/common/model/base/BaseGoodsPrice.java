package com.bytechainx.psi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseGoodsPrice<M extends BaseGoodsPrice<M>> extends Model<M> implements IBean {

	/**
	 * 主键
	 */
	public void setId(java.lang.Integer id) {
		set("id", id);
	}
	
	/**
	 * 主键
	 */
	public java.lang.Integer getId() {
		return getInt("id");
	}
	
	public void setGoodsInfoId(java.lang.Integer goodsInfoId) {
		set("goods_info_id", goodsInfoId);
	}
	
	public java.lang.Integer getGoodsInfoId() {
		return getInt("goods_info_id");
	}
	
	public void setBarCode(java.lang.String barCode) {
		set("bar_code", barCode);
	}
	
	public java.lang.String getBarCode() {
		return getStr("bar_code");
	}
	
	public void setSpec1Id(java.lang.Integer spec1Id) {
		set("spec_1_id", spec1Id);
	}
	
	public java.lang.Integer getSpec1Id() {
		return getInt("spec_1_id");
	}
	
	public void setSpecOption1Id(java.lang.Integer specOption1Id) {
		set("spec_option_1_id", specOption1Id);
	}
	
	public java.lang.Integer getSpecOption1Id() {
		return getInt("spec_option_1_id");
	}
	
	public void setSpec2Id(java.lang.Integer spec2Id) {
		set("spec_2_id", spec2Id);
	}
	
	public java.lang.Integer getSpec2Id() {
		return getInt("spec_2_id");
	}
	
	public void setSpecOption2Id(java.lang.Integer specOption2Id) {
		set("spec_option_2_id", specOption2Id);
	}
	
	public java.lang.Integer getSpecOption2Id() {
		return getInt("spec_option_2_id");
	}
	
	public void setSpec3Id(java.lang.Integer spec3Id) {
		set("spec_3_id", spec3Id);
	}
	
	public java.lang.Integer getSpec3Id() {
		return getInt("spec_3_id");
	}
	
	public void setSpecOption3Id(java.lang.Integer specOption3Id) {
		set("spec_option_3_id", specOption3Id);
	}
	
	public java.lang.Integer getSpecOption3Id() {
		return getInt("spec_option_3_id");
	}
	
	public void setUnitId(java.lang.Integer unitId) {
		set("unit_id", unitId);
	}
	
	public java.lang.Integer getUnitId() {
		return getInt("unit_id");
	}
	
	public void setCostPrice(java.math.BigDecimal costPrice) {
		set("cost_price", costPrice);
	}
	
	public java.math.BigDecimal getCostPrice() {
		return getBigDecimal("cost_price");
	}
	
	public void setAvgCostPrice(java.math.BigDecimal avgCostPrice) {
		set("avg_cost_price", avgCostPrice);
	}
	
	public java.math.BigDecimal getAvgCostPrice() {
		return getBigDecimal("avg_cost_price");
	}
	
	public void setInitPrice(java.math.BigDecimal initPrice) {
		set("init_price", initPrice);
	}
	
	public java.math.BigDecimal getInitPrice() {
		return getBigDecimal("init_price");
	}
	
	public void setSalePrice(java.lang.String salePrice) {
		set("sale_price", salePrice);
	}
	
	public java.lang.String getSalePrice() {
		return getStr("sale_price");
	}
	
	public void setDataStatus(java.lang.Integer dataStatus) {
		set("data_status", dataStatus);
	}
	
	public java.lang.Integer getDataStatus() {
		return getInt("data_status");
	}
	
	public void setRemark(java.lang.String remark) {
		set("remark", remark);
	}
	
	public java.lang.String getRemark() {
		return getStr("remark");
	}
	
	public void setCreatedAt(java.util.Date createdAt) {
		set("created_at", createdAt);
	}
	
	public java.util.Date getCreatedAt() {
		return getDate("created_at");
	}
	
	public void setUpdatedAt(java.util.Date updatedAt) {
		set("updated_at", updatedAt);
	}
	
	public java.util.Date getUpdatedAt() {
		return getDate("updated_at");
	}
	
}

