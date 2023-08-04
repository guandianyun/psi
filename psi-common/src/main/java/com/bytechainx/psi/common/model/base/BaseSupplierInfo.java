package com.bytechainx.psi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseSupplierInfo<M extends BaseSupplierInfo<M>> extends Model<M> implements IBean {

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
	
	public void setTraderBookAccountId(java.lang.Integer traderBookAccountId) {
		set("trader_book_account_id", traderBookAccountId);
	}
	
	public java.lang.Integer getTraderBookAccountId() {
		return getInt("trader_book_account_id");
	}
	
	public void setName(java.lang.String name) {
		set("name", name);
	}
	
	public java.lang.String getName() {
		return getStr("name");
	}
	
	public void setCode(java.lang.String code) {
		set("code", code);
	}
	
	public java.lang.String getCode() {
		return getStr("code");
	}
	
	public void setCustomerInfoId(java.lang.Integer customerInfoId) {
		set("customer_info_id", customerInfoId);
	}
	
	public java.lang.Integer getCustomerInfoId() {
		return getInt("customer_info_id");
	}
	
	public void setDefaultFlag(java.lang.Boolean defaultFlag) {
		set("default_flag", defaultFlag);
	}
	
	public java.lang.Boolean getDefaultFlag() {
		return getBoolean("default_flag");
	}
	
	public void setDataStatus(java.lang.Integer dataStatus) {
		set("data_status", dataStatus);
	}
	
	public java.lang.Integer getDataStatus() {
		return getInt("data_status");
	}
	
	public void setBlackFlag(java.lang.Boolean blackFlag) {
		set("black_flag", blackFlag);
	}
	
	public java.lang.Boolean getBlackFlag() {
		return getBoolean("black_flag");
	}
	
	public void setSupplierCategoryId(java.lang.Integer supplierCategoryId) {
		set("supplier_category_id", supplierCategoryId);
	}
	
	public java.lang.Integer getSupplierCategoryId() {
		return getInt("supplier_category_id");
	}
	
	public void setBuyerId(java.lang.Integer buyerId) {
		set("buyer_id", buyerId);
	}
	
	public java.lang.Integer getBuyerId() {
		return getInt("buyer_id");
	}
	
	public void setContact(java.lang.String contact) {
		set("contact", contact);
	}
	
	public java.lang.String getContact() {
		return getStr("contact");
	}
	
	public void setMobile(java.lang.String mobile) {
		set("mobile", mobile);
	}
	
	public java.lang.String getMobile() {
		return getStr("mobile");
	}
	
	public void setFirstOrderTime(java.util.Date firstOrderTime) {
		set("first_order_time", firstOrderTime);
	}
	
	public java.util.Date getFirstOrderTime() {
		return getDate("first_order_time");
	}
	
	public void setLastOrderTime(java.util.Date lastOrderTime) {
		set("last_order_time", lastOrderTime);
	}
	
	public java.util.Date getLastOrderTime() {
		return getDate("last_order_time");
	}
	
	public void setProvinceCode(java.lang.String provinceCode) {
		set("province_code", provinceCode);
	}
	
	public java.lang.String getProvinceCode() {
		return getStr("province_code");
	}
	
	public void setProvinceName(java.lang.String provinceName) {
		set("province_name", provinceName);
	}
	
	public java.lang.String getProvinceName() {
		return getStr("province_name");
	}
	
	public void setCityCode(java.lang.String cityCode) {
		set("city_code", cityCode);
	}
	
	public java.lang.String getCityCode() {
		return getStr("city_code");
	}
	
	public void setCityName(java.lang.String cityName) {
		set("city_name", cityName);
	}
	
	public java.lang.String getCityName() {
		return getStr("city_name");
	}
	
	public void setCountyCode(java.lang.String countyCode) {
		set("county_code", countyCode);
	}
	
	public java.lang.String getCountyCode() {
		return getStr("county_code");
	}
	
	public void setCountyName(java.lang.String countyName) {
		set("county_name", countyName);
	}
	
	public java.lang.String getCountyName() {
		return getStr("county_name");
	}
	
	public void setAddress(java.lang.String address) {
		set("address", address);
	}
	
	public java.lang.String getAddress() {
		return getStr("address");
	}
	
	public void setBankName(java.lang.String bankName) {
		set("bank_name", bankName);
	}
	
	public java.lang.String getBankName() {
		return getStr("bank_name");
	}
	
	public void setBankAccountName(java.lang.String bankAccountName) {
		set("bank_account_name", bankAccountName);
	}
	
	public java.lang.String getBankAccountName() {
		return getStr("bank_account_name");
	}
	
	public void setBankNo(java.lang.String bankNo) {
		set("bank_no", bankNo);
	}
	
	public java.lang.String getBankNo() {
		return getStr("bank_no");
	}
	
	public void setPosition(java.lang.Integer position) {
		set("position", position);
	}
	
	public java.lang.Integer getPosition() {
		return getInt("position");
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

