package com.bytechainx.psi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BasePurchaseRejectOrderFund<M extends BasePurchaseRejectOrderFund<M>> extends Model<M> implements IBean {

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
	
	public void setBalanceAccountId(java.lang.Integer balanceAccountId) {
		set("balance_account_id", balanceAccountId);
	}
	
	public java.lang.Integer getBalanceAccountId() {
		return getInt("balance_account_id");
	}
	
	public void setPayType(java.lang.Integer payType) {
		set("pay_type", payType);
	}
	
	public java.lang.Integer getPayType() {
		return getInt("pay_type");
	}
	
	public void setAmount(java.math.BigDecimal amount) {
		set("amount", amount);
	}
	
	public java.math.BigDecimal getAmount() {
		return getBigDecimal("amount");
	}
	
	public void setPayTime(java.util.Date payTime) {
		set("pay_time", payTime);
	}
	
	public java.util.Date getPayTime() {
		return getDate("pay_time");
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

