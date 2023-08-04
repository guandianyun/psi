package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.EnumConstant.ExportStatusEnum;
import com.bytechainx.psi.common.model.base.BaseTenantExportLog;

/**
 * 导出记录
 */
@SuppressWarnings("serial")
public class TenantExportLog extends BaseTenantExportLog<TenantExportLog> {
	
	public static final TenantExportLog dao = new TenantExportLog().dao();
	
	public String getExportStatusName() {
		return ExportStatusEnum.getEnum(getExportStatus()).getName();
	}
	
}

