package com.bytechainx.psi.sale.service;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.model.SaleOrder;
import com.bytechainx.psi.common.model.SaleOrderGoods;
import com.bytechainx.psi.common.model.SaleRejectOrder;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Page;


/**
* 销售统计
*/
public class StatSaleService extends CommonService {


	
	
	/**
	* 分页列表
	* 按客户统计
	 * @param customerInfoId2 
	 * @param pageSize2 
	*/
	public Page<SaleOrder> paginateByCustomer(Integer customerCategoryId, Integer customerInfoId, String startDay, String endDay, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		where.append(" and order_status = ?");
		params.add(OrderStatusEnum.normal.getValue());
		
		where.append(" and audit_status = ?");
		params.add(AuditStatusEnum.pass.getValue());
		
		if(StringUtils.isNotEmpty(startDay)) {
			where.append(" and order_time  >= ?");
			params.add(startDay);
		}
		if(StringUtils.isNotEmpty(endDay)) {
			where.append(" and order_time  <= ?");
			params.add(endDay);
		}
		StringBuffer rejectWhere = new StringBuffer(where);
		List<Object> rejectParams = new ArrayList<>();
		rejectParams.addAll(params);
		
		if(customerInfoId != null && customerInfoId > 0) {
			where.append(" and customer_info_id  = ?");
			params.add(customerInfoId);
		}
		if(customerCategoryId != null && customerCategoryId > 0) {
			where.append(" and customer_info_id  in (select id from customer_info where customer_category_id = ? and data_status = ?)");
			params.add(customerCategoryId);
			params.add(DataStatusEnum.enable.getValue());
		}
		Page<SaleOrder> page = SaleOrder.dao.paginate(pageNumber, pageSize, "select customer_info_id, sum(amount) as amount, sum(goods_cost_amount) as cost_amount, count(id) as order_count, sum(other_amount) as other_amount", "from sale_order "+where.toString()+" group by customer_info_id order by amount desc", params.toArray());
		for(SaleOrder saleOrder : page.getList()) {
			StringBuffer _rejectWhere = new StringBuffer(rejectWhere);
			List<Object> _rejectParams = new ArrayList<>();
			_rejectParams.addAll(rejectParams);
			_rejectWhere.append(" and customer_info_id  = ?");
			_rejectParams.add(saleOrder.getCustomerInfoId());
			
			SaleRejectOrder saleRejectOrder = SaleRejectOrder.dao.findFirst("select sum(amount) as amount, count(id) as order_count from sale_reject_order "+_rejectWhere+" limit 1", _rejectParams.toArray());
			saleOrder.setRejectAmount(saleRejectOrder.getAmount());
			saleOrder.put("reject_order_count", saleRejectOrder.getInt("order_count"));
		}
		return page;
	}
	
	/**
	* 分页列表
	* 按客户统计
	 * @param customerInfoId2 
	*/
	public SaleOrder sumByCustomer(Integer customerCategoryId, Integer customerInfoId, String startDay, String endDay) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		where.append(" and order_status = ?");
		params.add(OrderStatusEnum.normal.getValue());
		
		where.append(" and audit_status = ?");
		params.add(AuditStatusEnum.pass.getValue());
		
		if(StringUtils.isNotEmpty(startDay)) {
			where.append(" and order_time  >= ?");
			params.add(startDay);
		}
		if(StringUtils.isNotEmpty(endDay)) {
			where.append(" and order_time  <= ?");
			params.add(endDay);
		}
		if(customerInfoId != null && customerInfoId > 0) {
			where.append(" and customer_info_id  = ?");
			params.add(customerInfoId);
		}
		if(customerCategoryId != null && customerCategoryId > 0) {
			where.append(" and customer_info_id  in (select id from customer_info where customer_category_id = ? and data_status = ?)");
			params.add(customerCategoryId);
			params.add(DataStatusEnum.enable.getValue());
		}
		SaleOrder saleOrder = SaleOrder.dao.findFirst("select sum(amount) as amount, sum(paid_amount) as paid_amount, sum(goods_cost_amount) as cost_amount, count(id) as order_count, sum(other_amount) as other_amount from sale_order "+where.toString()+" limit 1", params.toArray());
		
		SaleRejectOrder saleRejectOrder = SaleRejectOrder.dao.findFirst("select sum(amount) as amount, sum(paid_amount) as paid_amount, count(id) as order_count from sale_reject_order "+where.toString()+" limit 1", params.toArray());
		saleOrder.setRejectAmount(saleRejectOrder.getAmount());
		saleOrder.put("reject_paid_amount", saleRejectOrder.getPaidAmount());
		saleOrder.put("reject_order_count", saleRejectOrder.getInt("order_count"));
		
		return saleOrder;
	}

	/**
	 * 查询客户的销售明细
	 * @param tenantOrgId
	 * @param startTime
	 * @param endTime
	 * @param condKv
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public Page<SaleOrderGoods> paginateByCustomerGoods(Integer customerInfoId, String startTime, String endTime, int pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");

		StringBuffer whereOrder = new StringBuffer();
		if(StringUtils.isNotEmpty(startTime)) {
			whereOrder.append(" order_time  >= '"+startTime+"' and ");
		}
		if(StringUtils.isNotEmpty(endTime)) {
			whereOrder.append(" order_time  <= '"+endTime+"' and ");
		}
		whereOrder.append("customer_info_id = "+customerInfoId+" and order_status = "+OrderStatusEnum.normal.getValue()+" and audit_status = "+AuditStatusEnum.pass.getValue());
		
		ConditionFilter filter = new ConditionFilter();
		filter.setOperator(Operator.in);
		filter.setValue("select id from sale_order where "+whereOrder.toString());
		Kv condKv = Kv.create();
		condKv.set("sale_order_id", filter);
		
		conditionFilter(condKv, where, params);

		return SaleOrderGoods.dao.paginate(pageNumber, pageSize, "select goods_info_id, sum(buy_number) as buy_number, sum(amount) as amount, sum(cost_amount) as cost_amount, sum(reject_number) as reject_number, sum(reject_amount) as reject_amount", "from sale_order_goods "+where.toString()+" group by goods_info_id order by buy_number desc", params.toArray());
	}


}