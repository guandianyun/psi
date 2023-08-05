package com.silkie.field;

/**
 * 公共域信息，所有的协议包都必须携带此域
 * @author defier
 * 2011-4-20 下午05:51:21
 * @since 1.0
 */
public class CommonProtocol {
	
	/**
	 * 返回编码
	 */
	private int rtnCode;
	/**
	 * 返回消息
	 */
	private String rtnMsg;
	
	public int getRtnCode() {
		return rtnCode;
	}

	public void setRtnCode(int rtnCode) {
		this.rtnCode = rtnCode;
	}

	public String getRtnMsg() {
		return rtnMsg;
	}

	public void setRtnMsg(String rtnMsg) {
		this.rtnMsg = rtnMsg;
	}
	

}
