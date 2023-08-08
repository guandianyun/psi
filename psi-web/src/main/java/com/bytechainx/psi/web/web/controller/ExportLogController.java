/**
 * 
 */
package com.bytechainx.psi.web.web.controller;

import java.util.Date;

import com.bytechainx.psi.common.EnumConstant.ExportStatusEnum;
import com.bytechainx.psi.common.model.TenantExportLog;
import com.bytechainx.psi.common.service.export.ExportLogService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.plugin.activerecord.Page;

/**
 * 导出任务信息
 * @author defier
 */
@Path("/export/log")
public class ExportLogController extends BaseController {
	
	@Inject
	private ExportLogService exportLogService;

	public void index() {
		keepPara("targetId");
	}
	
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		
		Page<TenantExportLog> page = exportLogService.paginate(getAdminId(), pageNumber, pageSize);
		for (TenantExportLog exportLog : page.getList()) {
			if(exportLog.getExportStatus() == ExportStatusEnum.ing.getValue() && exportLog.getCreatedAt().getTime() + (10*60*1000) <= System.currentTimeMillis()) {
				exportLog.setExportStatus(ExportStatusEnum.fail.getValue());
				exportLog.setErrorDesc("导出超时");
				exportLog.setUpdatedAt(new Date());
				exportLog.update();
			}
		}
		
		setAttr("page", page);
		keepPara("targetId");
	}
	

}
