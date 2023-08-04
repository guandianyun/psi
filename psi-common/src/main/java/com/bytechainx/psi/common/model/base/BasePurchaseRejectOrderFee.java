package com.bytechainx.psi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BasePurchaseRejectOrderFee<M extends BasePurchaseRejectOrderFee<M>> extends Model<M> implements IBean {

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
	
	public void setPurchaseRejectOrderId(java.lang.Integer purchaseRejectOrderId) {
		set("purchase_reject_order_id", purchaseRejectOrderId);
	}
	
	public java.lang.Integer getPurchaseRejectOrderId() {
		return getInt("purchase_reject_order_id");
	}
	
	public void setTraderFundType(java.lang.Integer traderFundType) {
		set("trader_fund_type", traderFundType);
	}
	
	public java.lang.Integer getTraderFundType() {
		return getInt("trader_fund_type");
	}
	
	public void setAmount(java.math.BigDecimal amount) {
		set("amount", amount);
	}
	
	public java.math.BigDecimal getAmount() {
		return getBigDecimal("amount");
	}
	
}

