package com.bytechainx.psi.purchase.service;


import java.util.ArrayList;
import java.util.List;

import com.bytechainx.psi.common.model.InventoryStock;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 库存预警
*/
public class StockWarningService extends CommonService {


	/**
	* 分页列表
	*/
	public Page<InventoryStock> paginate(Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");

		conditionFilter(conditionColumns, where, params);

		return InventoryStock.dao.paginate(pageNumber, pageSize, "select * ", "from inventory_stock "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 查看
	*/
	public Ret show() {

		return Ret.ok();
	}

}