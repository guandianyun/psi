package com.bytechainx.psi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BasePurchaseRejectOrder<M extends BasePurchaseRejectOrder<M>> extends Model<M> implements IBean {

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
	
	public void setPurchaseOrderId(java.lang.String purchaseOrderId) {
		set("purchase_order_id", purchaseOrderId);
	}
	
	public java.lang.String getPurchaseOrderId() {
		return getStr("purchase_order_id");
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
	
	public void setSupplierInfoId(java.lang.Integer supplierInfoId) {
		set("supplier_info_id", supplierInfoId);
	}
	
	public java.lang.Integer getSupplierInfoId() {
		return getInt("supplier_info_id");
	}
	
	public void setGoodsAmount(java.math.BigDecimal goodsAmount) {
		set("goods_amount", goodsAmount);
	}
	
	public java.math.BigDecimal getGoodsAmount() {
		return getBigDecimal("goods_amount");
	}
	
	public void setAmount(java.math.BigDecimal amount) {
		set("amount", amount);
	}
	
	public java.math.BigDecimal getAmount() {
		return getBigDecimal("amount");
	}
	
	public void setPaidAmount(java.math.BigDecimal paidAmount) {
		set("paid_amount", paidAmount);
	}
	
	public java.math.BigDecimal getPaidAmount() {
		return getBigDecimal("paid_amount");
	}
	
	public void setDiscountType(java.lang.Integer discountType) {
		set("discount_type", discountType);
	}
	
	public java.lang.Integer getDiscountType() {
		return getInt("discount_type");
	}
	
	public void setDiscount(java.math.BigDecimal discount) {
		set("discount", discount);
	}
	
	public java.math.BigDecimal getDiscount() {
		return getBigDecimal("discount");
	}
	
	public void setDiscountAmount(java.math.BigDecimal discountAmount) {
		set("discount_amount", discountAmount);
	}
	
	public java.math.BigDecimal getDiscountAmount() {
		return getBigDecimal("discount_amount");
	}
	
	public void setOddAmount(java.math.BigDecimal oddAmount) {
		set("odd_amount", oddAmount);
	}
	
	public java.math.BigDecimal getOddAmount() {
		return getBigDecimal("odd_amount");
	}
	
	public void setOtherAmount(java.math.BigDecimal otherAmount) {
		set("other_amount", otherAmount);
	}
	
	public java.math.BigDecimal getOtherAmount() {
		return getBigDecimal("other_amount");
	}
	
	public void setOtherCostAmount(java.math.BigDecimal otherCostAmount) {
		set("other_cost_amount", otherCostAmount);
	}
	
	public java.math.BigDecimal getOtherCostAmount() {
		return getBigDecimal("other_cost_amount");
	}
	
	public void setLogisticsAmount(java.math.BigDecimal logisticsAmount) {
		set("logistics_amount", logisticsAmount);
	}
	
	public java.math.BigDecimal getLogisticsAmount() {
		return getBigDecimal("logistics_amount");
	}
	
	public void setOrderStatus(java.lang.Integer orderStatus) {
		set("order_status", orderStatus);
	}
	
	public java.lang.Integer getOrderStatus() {
		return getInt("order_status");
	}
	
	public void setLogisticsStatus(java.lang.Integer logisticsStatus) {
		set("logistics_status", logisticsStatus);
	}
	
	public java.lang.Integer getLogisticsStatus() {
		return getInt("logistics_status");
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
	
	public void setPayStatus(java.lang.Integer payStatus) {
		set("pay_status", payStatus);
	}
	
	public java.lang.Integer getPayStatus() {
		return getInt("pay_status");
	}
	
	public void setOrderTime(java.util.Date orderTime) {
		set("order_time", orderTime);
	}
	
	public java.util.Date getOrderTime() {
		return getDate("order_time");
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
	
	public void setCancelRemark(java.lang.String cancelRemark) {
		set("cancel_remark", cancelRemark);
	}
	
	public java.lang.String getCancelRemark() {
		return getStr("cancel_remark");
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

