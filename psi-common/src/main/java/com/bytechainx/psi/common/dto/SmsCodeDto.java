/**
 * 
 */
package com.bytechainx.psi.common.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 短信验证码dto
 * @author defier
 *
 */
public class SmsCodeDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String code; // 验证码
	private Date sendTime; // 生成验证码的时间
	private String mobile; // 接收的手机号
	
	public SmsCodeDto(String code,Date sendTime,String mobile){
		this.code = code;
		this.sendTime = sendTime;
		this.mobile = mobile;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	

}
