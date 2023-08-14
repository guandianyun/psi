package com.bytechainx.psi.purchase.service;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.model.PurchaseOrder;
import com.bytechainx.psi.common.model.PurchaseOrderGoods;
import com.bytechainx.psi.common.model.PurchaseRejectOrder;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Page;


/**
* 进货统计
*/
public class StatPurchaseService extends CommonService {


	/**
	* 分页列表
	* 按供应商统计
	*/
	public Page<PurchaseOrder> paginateBySupplier(Integer supplierCategoryId, Integer supplierInfoId, String startDay, String endDay, Integer pageNumber, int pageSize) {
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
		
		if(supplierInfoId != null && supplierInfoId > 0) {
			where.append(" and supplier_info_id  = ?");
			params.add(supplierInfoId);
		}
		if(supplierCategoryId != null && supplierCategoryId > 0) {
			where.append(" and supplier_info_id  in (select id from supplier_info where supplier_category_id = ? and data_status = ?)");
			params.add(supplierCategoryId);
			params.add(DataStatusEnum.enable.getValue());
		}
		Page<PurchaseOrder> page = PurchaseOrder.dao.paginate(pageNumber, pageSize, "select supplier_info_id, sum(amount) as amount, count(id) as order_count, sum(other_amount) as other_amount", "from purchase_order "+where.toString()+" group by supplier_info_id order by amount desc", params.toArray());
		for(PurchaseOrder purchaseOrder : page.getList()) {
			StringBuffer _rejectWhere = new StringBuffer(rejectWhere);
			List<Object> _rejectParams = new ArrayList<>();
			_rejectParams.addAll(rejectParams);
			_rejectWhere.append(" and supplier_info_id  = ?");
			_rejectParams.add(purchaseOrder.getSupplierInfoId());
			
			PurchaseRejectOrder purchaseRejectOrder = PurchaseRejectOrder.dao.findFirst("select sum(amount) as amount, count(id) as order_count from purchase_reject_order "+_rejectWhere+" limit 1", _rejectParams.toArray());
			purchaseOrder.setRejectAmount(purchaseRejectOrder.getAmount());
			purchaseOrder.put("reject_order_count", purchaseRejectOrder.getInt("order_count"));
		}
		return page;
	}
	
	/**
	* 分页列表
	* 按供应商统计
	*/
	public PurchaseOrder sumBySupplier(Integer supplierCategoryId, Integer supplierInfoId, String startDay, String endDay) {
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
		if(supplierInfoId != null && supplierInfoId > 0) {
			where.append(" and supplier_info_id  = ?");
			params.add(supplierInfoId);
		}
		if(supplierCategoryId != null && supplierCategoryId > 0) {
			where.append(" and supplier_info_id  in (select id from supplier_info where supplier_category_id = ? and data_status = ?)");
			params.add(supplierCategoryId);
			params.add(DataStatusEnum.enable.getValue());
		}
		PurchaseOrder purchaseOrder = PurchaseOrder.dao.findFirst("select sum(amount) as amount, count(id) as order_count, sum(other_amount) as other_amount from purchase_order "+where.toString()+" limit 1", params.toArray());
		
		PurchaseRejectOrder purchaseRejectOrder = PurchaseRejectOrder.dao.findFirst("select sum(amount) as amount, count(id) as order_count from purchase_reject_order "+where.toString()+" limit 1", params.toArray());
		purchaseOrder.setRejectAmount(purchaseRejectOrder.getAmount());
		purchaseOrder.put("reject_paid_amount", purchaseRejectOrder.getPaidAmount());
		purchaseOrder.put("reject_order_count", purchaseRejectOrder.getInt("order_count"));
		
		return purchaseOrder;
	}

	/**
	 * 查询供应商的销售明细
	 * @param tenantOrgId
	 * @param startTime
	 * @param endTime
	 * @param condKv
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public Page<PurchaseOrderGoods> paginateBySupplierGoods(Integer supplierInfoId, String startTime, String endTime, int pageNumber, int pageSize) {
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
		whereOrder.append("supplier_info_id = "+supplierInfoId+" and order_status = "+OrderStatusEnum.normal.getValue()+" and audit_status = "+AuditStatusEnum.pass.getValue());
		
		ConditionFilter filter = new ConditionFilter();
		filter.setOperator(Operator.in);
		filter.setValue("select id from purchase_order where "+whereOrder.toString());
		Kv condKv = Kv.create();
		condKv.set("purchase_order_id", filter);
		
		conditionFilter(condKv, where, params);

		return PurchaseOrderGoods.dao.paginate(pageNumber, pageSize, "select goods_info_id, sum(buy_number) as buy_number, sum(amount) as amount, sum(reject_number) as reject_number, sum(reject_amount) as reject_amount", "from purchase_order_goods "+where.toString()+" group by goods_info_id order by buy_number desc", params.toArray());
	}
}