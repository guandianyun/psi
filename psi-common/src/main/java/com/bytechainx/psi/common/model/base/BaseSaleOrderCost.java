package com.bytechainx.psi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseSaleOrderCost<M extends BaseSaleOrderCost<M>> extends Model<M> implements IBean {

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
	
	public void setSaleOrderId(java.lang.Integer saleOrderId) {
		set("sale_order_id", saleOrderId);
	}
	
	public java.lang.Integer getSaleOrderId() {
		return getInt("sale_order_id");
	}
	
	public void setTraderFundType(java.lang.Integer traderFundType) {
		set("trader_fund_type", traderFundType);
	}
	
	public java.lang.Integer getTraderFundType() {
		return getInt("trader_fund_type");
	}
	
	public void setTraderBalanceAccountId(java.lang.Integer traderBalanceAccountId) {
		set("trader_balance_account_id", traderBalanceAccountId);
	}
	
	public java.lang.Integer getTraderBalanceAccountId() {
		return getInt("trader_balance_account_id");
	}
	
	public void setAmount(java.math.BigDecimal amount) {
		set("amount", amount);
	}
	
	public java.math.BigDecimal getAmount() {
		return getBigDecimal("amount");
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
	
}

