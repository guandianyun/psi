package com.bytechainx.psi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseInventoryChecking<M extends BaseInventoryChecking<M>> extends Model<M> implements IBean {

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
	
	public void setOrderCode(java.lang.String orderCode) {
		set("order_code", orderCode);
	}
	
	public java.lang.String getOrderCode() {
		return getStr("order_code");
	}
	
	public void setMakeManId(java.lang.Integer makeManId) {
		set("make_man_id", makeManId);
	}
	
	public java.lang.Integer getMakeManId() {
		return getInt("make_man_id");
	}
	
	public void setLastManId(java.lang.Integer lastManId) {
		set("last_man_id", lastManId);
	}
	
	public java.lang.Integer getLastManId() {
		return getInt("last_man_id");
	}
	
	public void setHandlerId(java.lang.Integer handlerId) {
		set("handler_id", handlerId);
	}
	
	public java.lang.Integer getHandlerId() {
		return getInt("handler_id");
	}
	
	public void setCheckType(java.lang.Integer checkType) {
		set("check_type", checkType);
	}
	
	public java.lang.Integer getCheckType() {
		return getInt("check_type");
	}
	
	public void setCheckNumber(java.math.BigDecimal checkNumber) {
		set("check_number", checkNumber);
	}
	
	public java.math.BigDecimal getCheckNumber() {
		return getBigDecimal("check_number");
	}
	
	public void setDifferNumber(java.math.BigDecimal differNumber) {
		set("differ_number", differNumber);
	}
	
	public java.math.BigDecimal getDifferNumber() {
		return getBigDecimal("differ_number");
	}
	
	public void setProfitLoss(java.math.BigDecimal profitLoss) {
		set("profit_loss", profitLoss);
	}
	
	public java.math.BigDecimal getProfitLoss() {
		return getBigDecimal("profit_loss");
	}
	
	public void setOrderTime(java.util.Date orderTime) {
		set("order_time", orderTime);
	}
	
	public java.util.Date getOrderTime() {
		return getDate("order_time");
	}
	
	public void setOrderStatus(java.lang.Integer orderStatus) {
		set("order_status", orderStatus);
	}
	
	public java.lang.Integer getOrderStatus() {
		return getInt("order_status");
	}
	
	public void setAuditStatus(java.lang.Integer auditStatus) {
		set("audit_status", auditStatus);
	}
	
	public java.lang.Integer getAuditStatus() {
		return getInt("audit_status");
	}
	
	public void setAuditorId(java.lang.Integer auditorId) {
		set("auditor_id", auditorId);
	}
	
	public java.lang.Integer getAuditorId() {
		return getInt("auditor_id");
	}
	
	public void setAuditTime(java.util.Date auditTime) {
		set("audit_time", auditTime);
	}
	
	public java.util.Date getAuditTime() {
		return getDate("audit_time");
	}
	
	public void setAuditDesc(java.lang.String auditDesc) {
		set("audit_desc", auditDesc);
	}
	
	public java.lang.String getAuditDesc() {
		return getStr("audit_desc");
	}
	
	public void setPrintCount(java.lang.Integer printCount) {
		set("print_count", printCount);
	}
	
	public java.lang.Integer getPrintCount() {
		return getInt("print_count");
	}
	
	public void setRemark(java.lang.String remark) {
		set("remark", remark);
	}
	
	public java.lang.String getRemark() {
		return getStr("remark");
	}
	
	public void setOrderImg(java.lang.String orderImg) {
		set("order_img", orderImg);
	}
	
	public java.lang.String getOrderImg() {
		return getStr("order_img");
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

