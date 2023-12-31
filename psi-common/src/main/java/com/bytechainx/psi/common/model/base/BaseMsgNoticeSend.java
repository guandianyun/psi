package com.bytechainx.psi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseMsgNoticeSend<M extends BaseMsgNoticeSend<M>> extends Model<M> implements IBean {

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
	
	public void setMsgNoticeId(java.lang.Integer msgNoticeId) {
		set("msg_notice_id", msgNoticeId);
	}
	
	public java.lang.Integer getMsgNoticeId() {
		return getInt("msg_notice_id");
	}
	
	public void setSenderId(java.lang.Integer senderId) {
		set("sender_id", senderId);
	}
	
	public java.lang.Integer getSenderId() {
		return getInt("sender_id");
	}
	
	public void setSenderName(java.lang.String senderName) {
		set("sender_name", senderName);
	}
	
	public java.lang.String getSenderName() {
		return getStr("sender_name");
	}
	
	public void setReceiverId(java.lang.Integer receiverId) {
		set("receiver_id", receiverId);
	}
	
	public java.lang.Integer getReceiverId() {
		return getInt("receiver_id");
	}
	
	public void setReadFlag(java.lang.Boolean readFlag) {
		set("read_flag", readFlag);
	}
	
	public java.lang.Boolean getReadFlag() {
		return getBoolean("read_flag");
	}
	
	public void setValidTime(java.util.Date validTime) {
		set("valid_time", validTime);
	}
	
	public java.util.Date getValidTime() {
		return getDate("valid_time");
	}
	
	public void setReadTime(java.util.Date readTime) {
		set("read_time", readTime);
	}
	
	public java.util.Date getReadTime() {
		return getDate("read_time");
	}
	
	public void setCreatedAt(java.util.Date createdAt) {
		set("created_at", createdAt);
	}
	
	public java.util.Date getCreatedAt() {
		return getDate("created_at");
	}
	
}

