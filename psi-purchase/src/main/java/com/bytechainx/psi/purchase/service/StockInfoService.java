package com.bytechainx.psi.purchase.service;


import java.util.ArrayList;
import java.util.List;

import com.bytechainx.psi.common.model.InventoryStock;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Page;


/**
* 库存查询
*/
public class StockInfoService extends CommonService {


	/**
	* 分页列表
	* 按商品查询
	*/
	public Page<InventoryStock> paginate(Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");

		conditionFilter(conditionColumns, where, params);

		return InventoryStock.dao.paginate(pageNumber, pageSize, "select tenant_org_id, goods_info_id, sum(stock) sum_stock, sum(reserve_stock) as sum_reserve_stock, sum(lock_stock) as sum_lock_stock ", "from inventory_stock "+where.toString()+" group by goods_info_id", params.toArray());
	}
	
	/**
	* 分页列表
	* 按规格查询
	*/
	public Page<InventoryStock> paginateBySpec(Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");

		conditionFilter(conditionColumns, where, params);

		return InventoryStock.dao.paginate(pageNumber, pageSize, "select * ", "from inventory_stock "+where.toString()+" order by id desc", params.toArray());
	}

}