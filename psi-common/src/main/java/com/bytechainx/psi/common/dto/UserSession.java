package com.bytechainx.psi.common.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.bytechainx.psi.common.Permissions;

/**
 * 用户session信息
 * 用户所属角色权限变更，需要更新对应的在线用户session
 * @author defier.lai
 */
public class UserSession implements Serializable {
	
	private static final long serialVersionUID = 1L;
	/**
	 * 是否超管
	 */
	private boolean superFlag;
	/**
	 * 用户ID
	 */
	private Integer tenantAdminId;
	/**
	 * 用户名
	 */
	private String realName;
	/**
	 * 最后登录IP
	 */
	private String loginIp;
	/**
	 * 最后登录时间
	 */
	private Date loginTime;
	/**
	 * 心跳时间
	 */
	private Date heartTime;
	/**
	 * 最后操作时间，如果超过10分钟未操作，则自动锁屏
	 */
	private Date lastOperTime;
	/**
	 * 员工类型
	 */
	private int usertype;
	
	public Set<String> getOperCodeSet() {
		return operCodeSet;
	}

	/**
	 * 操作列表
	 */
	private Set<String> operCodeSet = new HashSet<String>();

	public Integer getTenantAdminId() {
		return tenantAdminId;
	}

	public void setTenantAdminId(Integer tenantAdminId) {
		this.tenantAdminId = tenantAdminId;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public boolean isSuperFlag() {
		return superFlag;
	}

	public void setSuperFlag(boolean superFlag) {
		this.superFlag = superFlag;
	}
	public String getLoginIp() {
		return loginIp;
	}

	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}

	public boolean hasOper(Permissions code) {
		if(isSuperFlag()){
			return true;
		}
		if(code == null) {
			return false;
		}
		return operCodeSet.contains(code.name());
	}
	
	public boolean hasOper(Permissions... codes) {
		if(isSuperFlag()){
			return true;
		}
		if(codes == null || codes.length <= 0) {
			return false;
		}
		boolean f = false;
		for(Permissions code : codes) {
			if(hasOper(code)) {
				f = true;
				break;
			}
		}
		return f;
	}
	public boolean hasAnyOper(Permissions[] operCodes) {
		if(isSuperFlag()) {
			return true;
		}
		if(operCodes == null || operCodes.length <= 0) {
			return true;
		}
		boolean f = false;
		for(Permissions code : operCodes) {
			if(hasOper(code)) {
				f = true;
				break;
			}
		}
		return f;
	}

	public void setOperCodeSet(Set<String> operCodeSet) {
		this.operCodeSet = operCodeSet;
	}

	public int getUsertype() {
		return usertype;
	}

	public void setUsertype(int usertype) {
		this.usertype = usertype;
	}

	public Date getHeartTime() {
		return heartTime;
	}

	public void setHeartTime(Date heartTime) {
		this.heartTime = heartTime;
	}

	public Date getLastOperTime() {
		return lastOperTime;
	}

	public void setLastOperTime(Date lastOperTime) {
		this.lastOperTime = lastOperTime;
	}

}
