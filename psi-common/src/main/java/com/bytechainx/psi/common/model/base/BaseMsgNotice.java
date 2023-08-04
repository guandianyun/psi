package com.bytechainx.psi.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseMsgNotice<M extends BaseMsgNotice<M>> extends Model<M> implements IBean {

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
	
	public void setMsgLevel(java.lang.Integer msgLevel) {
		set("msg_level", msgLevel);
	}
	
	public java.lang.Integer getMsgLevel() {
		return getInt("msg_level");
	}
	
	public void setMsgType(java.lang.Integer msgType) {
		set("msg_type", msgType);
	}
	
	public java.lang.Integer getMsgType() {
		return getInt("msg_type");
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
	
	public void setTitle(java.lang.String title) {
		set("title", title);
	}
	
	public java.lang.String getTitle() {
		return getStr("title");
	}
	
	public void setContent(java.lang.String content) {
		set("content", content);
	}
	
	public java.lang.String getContent() {
		return getStr("content");
	}
	
	public void setDataType(java.lang.Integer dataType) {
		set("data_type", dataType);
	}
	
	public java.lang.Integer getDataType() {
		return getInt("data_type");
	}
	
	public void setCreatedAt(java.util.Date createdAt) {
		set("created_at", createdAt);
	}
	
	public java.util.Date getCreatedAt() {
		return getDate("created_at");
	}
	
}

