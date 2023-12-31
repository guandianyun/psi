package com.bytechainx.psi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseGoodsAttributeRef<M extends BaseGoodsAttributeRef<M>> extends Model<M> implements IBean {

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
	
	public void setGoodsAttributeId(java.lang.Integer goodsAttributeId) {
		set("goods_attribute_id", goodsAttributeId);
	}
	
	public java.lang.Integer getGoodsAttributeId() {
		return getInt("goods_attribute_id");
	}
	
	public void setAttrValue(java.lang.String attrValue) {
		set("attr_value", attrValue);
	}
	
	public java.lang.String getAttrValue() {
		return getStr("attr_value");
	}
	
	public void setPosition(java.lang.Integer position) {
		set("position", position);
	}
	
	public java.lang.Integer getPosition() {
		return getInt("position");
	}
	
}

