package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.EnumConstant.OperLogTypeEnum;
import com.bytechainx.psi.common.EnumConstant.PlatformTypeEnum;
import com.bytechainx.psi.common.model.base.BaseTenantOperLog;

/**
 * 操作日志
 */
@SuppressWarnings("serial")
public class TenantOperLog extends BaseTenantOperLog<TenantOperLog> {
	
	public static final TenantOperLog dao = new TenantOperLog().dao();

	
	public String getLogTypeName() {
		return OperLogTypeEnum.getEnum(getLogType()).getName();
	}
	
	public String getPlatformTypeName() {
		return PlatformTypeEnum.getEnum(getPlatformType()).getName();
	}
	
}

