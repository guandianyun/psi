package com.bytechainx.psi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseGoodsSpecOptions<M extends BaseGoodsSpecOptions<M>> extends Model<M> implements IBean {

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
	
	public void setGoodsSpecId(java.lang.Integer goodsSpecId) {
		set("goods_spec_id", goodsSpecId);
	}
	
	public java.lang.Integer getGoodsSpecId() {
		return getInt("goods_spec_id");
	}
	
	public void setOptionValue(java.lang.String optionValue) {
		set("option_value", optionValue);
	}
	
	public java.lang.String getOptionValue() {
		return getStr("option_value");
	}
	
	public void setDataStatus(java.lang.Integer dataStatus) {
		set("data_status", dataStatus);
	}
	
	public java.lang.Integer getDataStatus() {
		return getInt("data_status");
	}
	
}

