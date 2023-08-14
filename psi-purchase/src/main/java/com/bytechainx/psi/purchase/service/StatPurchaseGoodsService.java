package com.bytechainx.psi.purchase.service;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.model.PurchaseOrderGoods;
import com.bytechainx.psi.common.model.PurchaseRejectOrderGoods;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Page;


/**
* 进货商品分析
*/
public class StatPurchaseGoodsService extends CommonService {


	/**
	* 分页列表
	* 按商品统计
	*/
	public Page<PurchaseOrderGoods> paginate(Integer goodsCategoryId, String startTime, String endTime, Integer pageNumber, int pageSize) {
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
		filter.setValue("select id from purchase_order where "+whereOrder.toString());
		Kv condKv = Kv.create();
		condKv.set("purchase_order_id", filter);
		
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

		Page<PurchaseOrderGoods> page = PurchaseOrderGoods.dao.paginate(pageNumber, pageSize, "select goods_info_id, sum(buy_number) as buy_number, sum(amount) as amount, sum(reject_number) as reject_number, sum(reject_amount) as reject_amount", "from purchase_order_goods "+where.toString()+" group by goods_info_id order by buy_number desc", params.toArray());
		for(PurchaseOrderGoods purchaseOrderGoods : page.getList()) {
			StringBuffer rejectWhere = new StringBuffer();
			List<Object> rejectParams = new ArrayList<>();
			rejectWhere.append("where 1 = 1 ");
			rejectWhere.append(" and purchase_reject_order_id in (select id from purchase_reject_order where "+whereOrder.toString()+")");
			
			if(goodsCategoryId != null && goodsCategoryId > 0) {
				rejectWhere.append(" and goods_info_id in (select id from goods_info where goods_category_id = ? )");
				rejectParams.add(goodsCategoryId);
			}
			rejectWhere.append(" and goods_info_id = ?");
			rejectParams.add(purchaseOrderGoods.getGoodsInfoId());
			
			PurchaseRejectOrderGoods purchaseRejectOrderGoods = PurchaseRejectOrderGoods.dao.findFirst("select sum(buy_number) as buy_number, sum(amount) as amount from purchase_reject_order_goods "+rejectWhere.toString()+" limit 1", rejectParams.toArray());
			purchaseOrderGoods.setRejectAmount(purchaseRejectOrderGoods.getAmount());
			purchaseOrderGoods.setRejectNumber(purchaseRejectOrderGoods.getBuyNumber());
		}
		return page;
	}
	
	/**
	* 分页列表
	* 按商品分类统计
	*/
	public PurchaseOrderGoods sumByCategory(Integer goodsCategoryId, String startTime, String endTime) {
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
		filter.setValue("select id from purchase_order where "+whereOrder.toString());
		Kv condKv = Kv.create();
		condKv.set("purchase_order_id", filter);
		
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

		PurchaseOrderGoods purchaseOrderGoods = PurchaseOrderGoods.dao.findFirst("select sum(buy_number) as buy_number, sum(amount) as amount from purchase_order_goods "+where.toString(), params.toArray());	
		
		StringBuffer rejectWhere = new StringBuffer();
		List<Object> rejectParams = new ArrayList<>();
		rejectWhere.append("where 1 = 1 ");
		rejectWhere.append(" and purchase_reject_order_id in (select id from purchase_reject_order where "+whereOrder.toString()+")");
		
		if(goodsCategoryId != null && goodsCategoryId > 0) {
			rejectWhere.append(" and goods_info_id in (select id from goods_info where goods_category_id = ? )");
			rejectParams.add(goodsCategoryId);
		}
		PurchaseRejectOrderGoods purchaseRejectOrderGoods = PurchaseRejectOrderGoods.dao.findFirst("select sum(buy_number) as buy_number, sum(amount) as amount from purchase_reject_order_goods "+rejectWhere.toString()+" limit 1", rejectParams.toArray());
		purchaseOrderGoods.setRejectAmount(purchaseRejectOrderGoods.getAmount());
		purchaseOrderGoods.setRejectNumber(purchaseRejectOrderGoods.getBuyNumber());
		
		return purchaseOrderGoods;
	}
	
	/**
	 * 查询货品的查询明细
	 * @return
	 */
	public Page<PurchaseOrderGoods> paginateByGoods(Integer goodsInfoId, String startTime, String endTime, int pageNumber, int pageSize) {
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
		
		where.append(" and purchase_order_id  in (select id from purchase_order where "+whereOrder.toString()+")");
		
		if(goodsInfoId != null && goodsInfoId > 0) {
			where.append(" and goods_info_id  = ?");
			params.add(goodsInfoId);
		}

		return PurchaseOrderGoods.dao.paginate(pageNumber, pageSize, "select * ", "from purchase_order_goods "+where.toString()+" order by id", params.toArray());
	}

	
	/**
	* 分页列表
	* 按规格明细统计
	*/
	public Page<PurchaseOrderGoods> paginateBySpec(String startDay, String endDay, Integer pageNumber, int pageSize) {
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
		
		return PurchaseOrderGoods.dao.paginate(pageNumber, pageSize, "select goods_info_id,spec_1_id,spec_option_1_id,spec_2_id,spec_option_2_id,spec_3_id,spec_option_3_id,unit_id, sum(buy_number) as sum_buy_number ", "from purchase_order_goods "+where.toString()+" group by goods_info_id,spec_1_id,spec_option_1_id,spec_2_id,spec_option_2_id,spec_3_id,spec_option_3_id,unit_id order by id desc", params.toArray());
	}

	public Page<PurchaseOrderGoods> paginateBySupplier(Integer goodsInfoId, String startTime, String endTime, int pageNumber, int pageSize) {
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
		
		where.append(" and purchase_order_id  in (select id from purchase_order where "+whereOrder.toString()+")");
		
		if(goodsInfoId != null && goodsInfoId > 0) {
			where.append(" and goods_info_id  = ?");
			params.add(goodsInfoId);
		}
		
		Page<PurchaseOrderGoods> page = PurchaseOrderGoods.dao.paginate(pageNumber, pageSize, "select supplier_info_id, sum(buy_number) as buy_number, sum(amount) as amount ", "from purchase_order_goods "+where.toString()+" group by supplier_info_id order by buy_number desc", params.toArray());
		for (PurchaseOrderGoods purchaseOrderGoods : page.getList()) {
			StringBuffer rejectWhere = new StringBuffer();
			List<Object> rejectParams = new ArrayList<>();
			rejectWhere.append("where 1 = 1 ");
			rejectWhere.append(" and purchase_reject_order_id in (select id from purchase_reject_order where "+whereOrder.toString()+")");
			rejectWhere.append(" and goods_info_id = ?");
			rejectParams.add(goodsInfoId);
			rejectWhere.append(" and supplier_info_id = ?");
			rejectParams.add(purchaseOrderGoods.getSupplierInfoId());
			
			PurchaseRejectOrderGoods purchaseRejectOrderGoods = PurchaseRejectOrderGoods.dao.findFirst("select sum(buy_number) as buy_number, sum(amount) as amount from purchase_reject_order_goods "+rejectWhere.toString()+" limit 1", rejectParams.toArray());
			purchaseOrderGoods.setRejectAmount(purchaseRejectOrderGoods.getAmount());
			purchaseOrderGoods.setRejectNumber(purchaseRejectOrderGoods.getBuyNumber());
		}
		return page;
	}

	public PurchaseOrderGoods sumByGoodsId(Integer goodsInfoId, String startTime, String endTime) {
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
		filter.setValue("select id from purchase_order where "+whereOrder.toString());
		
		Kv condKv = Kv.create();
		condKv.set("purchase_order_id", filter);
		condKv.set("goods_info_id", goodsInfoId);
		
		conditionFilter(condKv, where, params);

		PurchaseOrderGoods purchaseOrderGoods = PurchaseOrderGoods.dao.findFirst("select sum(buy_number) as buy_number, sum(amount) as amount, sum(reject_number) as reject_number, sum(reject_amount) as reject_amount from purchase_order_goods "+where.toString(), params.toArray());	
		
		StringBuffer rejectWhere = new StringBuffer();
		List<Object> rejectParams = new ArrayList<>();
		rejectWhere.append("where 1 = 1 ");
		rejectWhere.append(" and purchase_reject_order_id in (select id from purchase_reject_order where "+whereOrder.toString()+")");
		rejectWhere.append(" and goods_info_id = ?");
		rejectParams.add(goodsInfoId);
		
		PurchaseRejectOrderGoods purchaseRejectOrderGoods = PurchaseRejectOrderGoods.dao.findFirst("select sum(buy_number) as buy_number, sum(amount) as amount from purchase_reject_order_goods "+rejectWhere.toString()+" limit 1", rejectParams.toArray());
		purchaseOrderGoods.setRejectAmount(purchaseRejectOrderGoods.getAmount());
		purchaseOrderGoods.setRejectNumber(purchaseRejectOrderGoods.getBuyNumber());
		
		return purchaseOrderGoods;
	}

}