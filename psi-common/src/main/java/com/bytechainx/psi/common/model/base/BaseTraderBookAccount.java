package com.bytechainx.psi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseTraderBookAccount<M extends BaseTraderBookAccount<M>> extends Model<M> implements IBean {

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
	
	public void setInAmount(java.math.BigDecimal inAmount) {
		set("in_amount", inAmount);
	}
	
	public java.math.BigDecimal getInAmount() {
		return getBigDecimal("in_amount");
	}
	
	public void setOutAmount(java.math.BigDecimal outAmount) {
		set("out_amount", outAmount);
	}
	
	public java.math.BigDecimal getOutAmount() {
		return getBigDecimal("out_amount");
	}
	
	public void setPayAmount(java.math.BigDecimal payAmount) {
		set("pay_amount", payAmount);
	}
	
	public java.math.BigDecimal getPayAmount() {
		return getBigDecimal("pay_amount");
	}
	
	public void setOpenBalance(java.math.BigDecimal openBalance) {
		set("open_balance", openBalance);
	}
	
	public java.math.BigDecimal getOpenBalance() {
		return getBigDecimal("open_balance");
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

