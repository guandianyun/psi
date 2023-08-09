package com.bytechainx.psi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseTraderPayOrderRef<M extends BaseTraderPayOrderRef<M>> extends Model<M> implements IBean {

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
	
	public void setTraderPayOrderId(java.lang.Integer traderPayOrderId) {
		set("trader_pay_order_id", traderPayOrderId);
	}
	
	public java.lang.Integer getTraderPayOrderId() {
		return getInt("trader_pay_order_id");
	}
	
	public void setPurchaseOrderId(java.lang.Integer purchaseOrderId) {
		set("purchase_order_id", purchaseOrderId);
	}
	
	public java.lang.Integer getPurchaseOrderId() {
		return getInt("purchase_order_id");
	}
	
	public void setAmount(java.math.BigDecimal amount) {
		set("amount", amount);
	}
	
	public java.math.BigDecimal getAmount() {
		return getBigDecimal("amount");
	}
	
	public void setDiscountAmount(java.math.BigDecimal discountAmount) {
		set("discount_amount", discountAmount);
	}
	
	public java.math.BigDecimal getDiscountAmount() {
		return getBigDecimal("discount_amount");
	}
	
}
