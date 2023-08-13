package com.bytechainx.psi.sale.service;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.model.SaleOrderGoods;
import com.bytechainx.psi.common.model.SaleRejectOrderGoods;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Page;


/**
* 热销分析
*/
public class StatHotSaleService extends CommonService {


	/**
	* 分页列表
	* 按商品统计
	*/
	public Page<SaleOrderGoods> paginate(Integer goodsCategoryId, String startTime, String endTime, Integer pageNumber, int pageSize) {
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
		whereOrder.append(" order_status = "+OrderStatusEnum.normal.getValue()+" and audit_status = "+AuditStatusEnum.pass.getValue());
		
		ConditionFilter filter = new ConditionFilter();
		filter.setOperator(Operator.in);
		filter.setValue("select id from sale_order where "+whereOrder.toString());
		Kv condKv = Kv.create();
		condKv.set("sale_order_id", filter);
		
		if(goodsCategoryId != null && goodsCategoryId > 0) {
			StringBuffer whereCategory = new StringBuffer();
			whereCategory.append("where 1 = 1 ");
			whereCategory.append(" and goods_category_id = "+goodsCategoryId);
			ConditionFilter categoryFilter = new ConditionFilter();
			categoryFilter.setOperator(Operator.in);
			categoryFilter.setValue("select id from goods_info "+whereCategory.toString());
			condKv.set("goods_info_id", categoryFilter);
		}
		
		conditionFilter(condKv, where, params);

		Page<SaleOrderGoods> page = SaleOrderGoods.dao.paginate(pageNumber, pageSize, "select goods_info_id, sum(buy_number) as buy_number, sum(amount) as amount, sum(cost_amount) as cost_amount, sum(reject_number) as reject_number, sum(reject_amount) as reject_amount", "from sale_order_goods "+where.toString()+" group by goods_info_id order by buy_number desc", params.toArray());
		for(SaleOrderGoods saleOrderGoods : page.getList()) {
			StringBuffer rejectWhere = new StringBuffer();
			List<Object> rejectParams = new ArrayList<>();
			rejectWhere.append("where 1 = 1 ");
			rejectWhere.append(" and sale_reject_order_id in (select id from sale_reject_order where "+whereOrder.toString()+")");
			
			if(goodsCategoryId != null && goodsCategoryId > 0) {
				rejectWhere.append(" and goods_info_id in (select id from goods_info where goods_category_id = ? )");
				rejectParams.add(goodsCategoryId);
			}
			rejectWhere.append(" and goods_info_id = ?");
			rejectParams.add(saleOrderGoods.getGoodsInfoId());
			
			SaleRejectOrderGoods saleRejectOrderGoods = SaleRejectOrderGoods.dao.findFirst("select sum(buy_number) as buy_number, sum(amount) as amount from sale_reject_order_goods "+rejectWhere.toString()+" limit 1", rejectParams.toArray());
			saleOrderGoods.setRejectAmount(saleRejectOrderGoods.getAmount());
			saleOrderGoods.setRejectNumber(saleRejectOrderGoods.getBuyNumber());
		}
		return page;
	}
	
	/**
	* 分页列表
	* 按商品分类统计
	*/
	public SaleOrderGoods sumByCategory(Integer goodsCategoryId, String startTime, String endTime) {
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
		whereOrder.append("order_status = "+OrderStatusEnum.normal.getValue()+" and audit_status = "+AuditStatusEnum.pass.getValue());
		
		ConditionFilter filter = new ConditionFilter();
		filter.setOperator(Operator.in);
		filter.setValue("select id from sale_order where "+whereOrder.toString());
		Kv condKv = Kv.create();
		condKv.set("sale_order_id", filter);
		
		StringBuffer whereCategory = new StringBuffer();
		whereCategory.append("where 1 = 1 ");
		if(goodsCategoryId != null && goodsCategoryId > 0) {
			whereCategory.append(" and goods_category_id = "+goodsCategoryId);
		}
		
		ConditionFilter categoryFilter = new ConditionFilter();
		categoryFilter.setOperator(Operator.in);
		categoryFilter.setValue("select id from goods_info "+whereCategory.toString());
		condKv.set("goods_info_id", categoryFilter);
		
		conditionFilter(condKv, where, params);

		SaleOrderGoods saleOrderGoods = SaleOrderGoods.dao.findFirst("select sum(buy_number) as buy_number, sum(amount) as amount, sum(cost_amount) as cost_amount from sale_order_goods "+where.toString(), params.toArray());	
		
		StringBuffer rejectWhere = new StringBuffer();
		List<Object> rejectParams = new ArrayList<>();
		rejectWhere.append("where 1 = 1 ");
		rejectWhere.append(" and sale_reject_order_id in (select id from sale_reject_order where "+whereOrder.toString()+")");
		
		if(goodsCategoryId != null && goodsCategoryId > 0) {
			rejectWhere.append(" and goods_info_id in (select id from goods_info where goods_category_id = ? )");
			rejectParams.add(goodsCategoryId);
		}
		SaleRejectOrderGoods saleRejectOrderGoods = SaleRejectOrderGoods.dao.findFirst("select sum(buy_number) as buy_number, sum(amount) as amount from sale_reject_order_goods "+rejectWhere.toString()+" limit 1", rejectParams.toArray());
		saleOrderGoods.setRejectAmount(saleRejectOrderGoods.getAmount());
		saleOrderGoods.setRejectNumber(saleRejectOrderGoods.getBuyNumber());
		
		return saleOrderGoods;
	}
	
	/**
	 * 查询货品的查询明细
	 * @return
	 */
	public Page<SaleOrderGoods> paginateByGoods(Integer goodsInfoId, String startTime, String endTime, int pageNumber, int pageSize) {
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
		whereOrder.append("order_status = "+OrderStatusEnum.normal.getValue()+" and audit_status = "+AuditStatusEnum.pass.getValue());
		
		where.append(" and sale_order_id  in (select id from sale_order where "+whereOrder.toString()+")");
		
		if(goodsInfoId != null && goodsInfoId > 0) {
			where.append(" and goods_info_id  = ?");
			params.add(goodsInfoId);
		}

		return SaleOrderGoods.dao.paginate(pageNumber, pageSize, "select * ", "from sale_order_goods "+where.toString()+" order by id", params.toArray());
	}

	
	/**
	* 分页列表
	* 按规格明细统计
	*/
	public Page<SaleOrderGoods> paginateBySpec(String startDay, String endDay, Integer pageNumber, int pageSize) {
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
		return SaleOrderGoods.dao.paginate(pageNumber, pageSize, "select goods_info_id,spec_1_id,spec_option_1_id,spec_2_id,spec_option_2_id,spec_3_id,spec_option_3_id,unit_id, sum(buy_number) as sum_buy_number ", "from sale_order_goods "+where.toString()+" group by goods_info_id,spec_1_id,spec_option_1_id,spec_2_id,spec_option_2_id,spec_3_id,spec_option_3_id,unit_id order by id desc", params.toArray());
	}

	public Page<SaleOrderGoods> paginateByCustomer(Integer goodsInfoId, String startTime, String endTime, int pageNumber, int pageSize) {
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
		whereOrder.append("order_status = "+OrderStatusEnum.normal.getValue()+" and audit_status = "+AuditStatusEnum.pass.getValue());
		
		where.append(" and sale_order_id  in (select id from sale_order where "+whereOrder.toString()+")");
		
		if(goodsInfoId != null && goodsInfoId > 0) {
			where.append(" and goods_info_id  = ?");
			params.add(goodsInfoId);
		}
		
		Page<SaleOrderGoods> page = SaleOrderGoods.dao.paginate(pageNumber, pageSize, "select customer_info_id, sum(buy_number) as buy_number, sum(amount) as amount, sum(cost_amount) as cost_amount ", "from sale_order_goods "+where.toString()+" group by customer_info_id order by buy_number desc", params.toArray());
		for (SaleOrderGoods saleOrderGoods : page.getList()) {
			StringBuffer rejectWhere = new StringBuffer();
			List<Object> rejectParams = new ArrayList<>();
			rejectWhere.append("where 1 = 1 ");
			rejectWhere.append(" and sale_reject_order_id in (select id from sale_reject_order where "+whereOrder.toString()+")");
			rejectWhere.append(" and goods_info_id = ?");
			rejectParams.add(goodsInfoId);
			rejectWhere.append(" and customer_info_id = ?");
			rejectParams.add(saleOrderGoods.getCustomerInfoId());
			
			SaleRejectOrderGoods saleRejectOrderGoods = SaleRejectOrderGoods.dao.findFirst("select sum(buy_number) as buy_number, sum(amount) as amount from sale_reject_order_goods "+rejectWhere.toString()+" limit 1", rejectParams.toArray());
			saleOrderGoods.setRejectAmount(saleRejectOrderGoods.getAmount());
			saleOrderGoods.setRejectNumber(saleRejectOrderGoods.getBuyNumber());
		}
		return page;
	}

	public SaleOrderGoods sumByGoodsId(Integer goodsInfoId, String startTime, String endTime) {
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
		whereOrder.append("order_status = "+OrderStatusEnum.normal.getValue()+" and audit_status = "+AuditStatusEnum.pass.getValue());
		
		ConditionFilter filter = new ConditionFilter();
		filter.setOperator(Operator.in);
		filter.setValue("select id from sale_order where "+whereOrder.toString());
		
		Kv condKv = Kv.create();
		condKv.set("sale_order_id", filter);
		condKv.set("goods_info_id", goodsInfoId);
		
		conditionFilter(condKv, where, params);

		SaleOrderGoods saleOrderGoods = SaleOrderGoods.dao.findFirst("select sum(buy_number) as buy_number, sum(amount) as amount, sum(cost_amount) as cost_amount, sum(reject_number) as reject_number, sum(reject_amount) as reject_amount from sale_order_goods "+where.toString(), params.toArray());	
		
		StringBuffer rejectWhere = new StringBuffer();
		List<Object> rejectParams = new ArrayList<>();
		rejectWhere.append("where 1 = 1 ");
		rejectWhere.append(" and sale_reject_order_id in (select id from sale_reject_order where "+whereOrder.toString()+")");
		rejectWhere.append(" and goods_info_id = ?");
		rejectParams.add(goodsInfoId);
		
		SaleRejectOrderGoods saleRejectOrderGoods = SaleRejectOrderGoods.dao.findFirst("select sum(buy_number) as buy_number, sum(amount) as amount from sale_reject_order_goods "+rejectWhere.toString()+" limit 1", rejectParams.toArray());
		saleOrderGoods.setRejectAmount(saleRejectOrderGoods.getAmount());
		saleOrderGoods.setRejectNumber(saleRejectOrderGoods.getBuyNumber());
		
		return saleOrderGoods;
	}

}