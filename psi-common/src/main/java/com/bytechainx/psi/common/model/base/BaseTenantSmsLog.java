package com.bytechainx.psi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseTenantSmsLog<M extends BaseTenantSmsLog<M>> extends Model<M> implements IBean {

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
	
	public void setContent(java.lang.String content) {
		set("content", content);
	}
	
	public java.lang.String getContent() {
		return getStr("content");
	}
	
	public void setUserInfoId(java.lang.Integer userInfoId) {
		set("user_info_id", userInfoId);
	}
	
	public java.lang.Integer getUserInfoId() {
		return getInt("user_info_id");
	}
	
	public void setMobile(java.lang.String mobile) {
		set("mobile", mobile);
	}
	
	public java.lang.String getMobile() {
		return getStr("mobile");
	}
	
	public void setSendFlag(java.lang.Boolean sendFlag) {
		set("send_flag", sendFlag);
	}
	
	public java.lang.Boolean getSendFlag() {
		return getBoolean("send_flag");
	}
	
	public void setRetryCount(java.lang.Integer retryCount) {
		set("retry_count", retryCount);
	}
	
	public java.lang.Integer getRetryCount() {
		return getInt("retry_count");
	}
	
	public void setSendDesc(java.lang.String sendDesc) {
		set("send_desc", sendDesc);
	}
	
	public java.lang.String getSendDesc() {
		return getStr("send_desc");
	}
	
	public void setCreatedAt(java.util.Date createdAt) {
		set("created_at", createdAt);
	}
	
	public java.util.Date getCreatedAt() {
		return getDate("created_at");
	}
	
}

