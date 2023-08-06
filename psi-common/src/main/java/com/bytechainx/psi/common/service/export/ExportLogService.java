/**
 * 
 */
package com.bytechainx.psi.common.service.export;

import java.util.ArrayList;
import java.util.List;

import com.bytechainx.psi.common.model.TenantExportLog;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.plugin.activerecord.Page;

/**
 * 导出记录
 * @author defier
 *
 */
public class ExportLogService extends CommonService {
	
	public Page<TenantExportLog> paginate(Integer handlerId, int pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 =1 ");
		if(handlerId != null && handlerId > 0) {
			where.append(" and handler_id = ?");
			params.add(handlerId);
		}
		return TenantExportLog.dao.paginate(pageNumber, pageSize, "select * ", "from tenant_export_log "+where.toString()+" order by id desc", params.toArray());
	}
	

}
