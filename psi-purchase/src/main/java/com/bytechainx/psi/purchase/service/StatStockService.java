package com.bytechainx.psi.purchase.service;


import java.util.ArrayList;
import java.util.List;

import com.bytechainx.psi.common.model.InventoryStockLog;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 库存统计
*/
public class StatStockService extends CommonService {


	/**
	* 库存流水
	*/
	public Page<InventoryStockLog> paginate(Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		
		conditionFilter(conditionColumns, where, params);

		return InventoryStockLog.dao.paginate(pageNumber, pageSize, "select * ", "from inventory_stock_log "+where.toString()+"  order by id desc", params.toArray());
	}

	/**
	* 查看
	*/
	public Ret show() {

		return Ret.ok();
	}

}