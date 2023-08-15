package com.bytechainx.psi.fund.service;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.model.TraderFundOrder;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Page;


/**
* 资金流水
*/
public class StatFundOrderService extends CommonService {


	/**
	* 分页列表
	*/
	public Page<TraderFundOrder> paginate(String startDay, String endDay, Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		if(StringUtils.isNotEmpty(startDay)) {
			where.append(" and order_time  >= ?");
			params.add(startDay);
		}
		if(StringUtils.isNotEmpty(endDay)) {
			where.append(" and order_time  <= ?");
			params.add(endDay);
		}

		conditionFilter(conditionColumns, where, params);

		return TraderFundOrder.dao.paginate(pageNumber, pageSize, "select * ", "from trader_fund_order "+where.toString()+" order by id desc", params.toArray());
	}

	public TraderFundOrder sumAmount(String startDay, String endDay, Kv condKv) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		if(StringUtils.isNotEmpty(startDay)) {
			where.append(" and order_time  >= ?");
			params.add(startDay);
		}
		if(StringUtils.isNotEmpty(endDay)) {
			where.append(" and order_time  <= ?");
			params.add(endDay);
		}
		conditionFilter(condKv, where, params);
		
		return TraderFundOrder.dao.findFirst("select sum(amount) as amount from trader_fund_order "+where.toString()+" order by id desc", params.toArray());
	}

}