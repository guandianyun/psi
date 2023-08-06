package com.bytechainx.psi.common.service.setting;


import java.util.ArrayList;
import java.util.List;

import com.bytechainx.psi.common.model.TenantPayOrder;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Page;


/**
* 在线支付
*/
public class TenantPayOrderService extends CommonService {


	/**
	* 分页列表
	*/
	public Page<TenantPayOrder> paginate(Kv condKv, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		
		conditionFilter(condKv, where, params);
		
		return TenantPayOrder.dao.paginate(pageNumber, pageSize, "select * ", "from tenant_pay_order "+where.toString()+" order by id", params.toArray());
	}

	
}