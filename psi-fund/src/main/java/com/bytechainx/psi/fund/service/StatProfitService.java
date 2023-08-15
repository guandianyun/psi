package com.bytechainx.psi.fund.service;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.model.InventoryChecking;
import com.bytechainx.psi.common.model.PurchaseOrder;
import com.bytechainx.psi.common.model.PurchaseRejectOrder;
import com.bytechainx.psi.common.model.SaleOrder;
import com.bytechainx.psi.common.model.SaleRejectOrder;
import com.bytechainx.psi.common.model.TraderIncomeExpenses;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;


/**
* 经营利润
*/
public class StatProfitService extends CommonService {

	/**
	 * 统计销售单
	 * @return
	 */
	public SaleOrder sumSaleOrder(String startTime, String endTime, Kv condKv) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		where.append(" and order_status = ?");
		params.add(OrderStatusEnum.normal.getValue());
		where.append(" and audit_status = ?");
		params.add(AuditStatusEnum.pass.getValue());
		
		if(StringUtils.isNotEmpty(startTime)) {
			where.append(" and order_time  >= ?");
			params.add(startTime);
		}
		if(StringUtils.isNotEmpty(endTime)) {
			where.append(" and order_time  <= ?");
			params.add(endTime);
		}
		
		conditionFilter(condKv, where, params);
		
		return SaleOrder.dao.findFirstByCache(CommonConstant.CACHE_NAME_FIVE_MINUTE_STORE, "sale.order."+where.toString()+"."+params.toString(), "select ifnull(sum(amount),0) as amount, ifnull(sum(other_cost_amount),0) as other_cost_amount, ifnull(sum(paid_amount),0) as paid_amount from sale_order "+where.toString(), params.toArray());
	}
	
	/**
	 * 统计销售退货单
	 * @return
	 */
	public SaleRejectOrder sumSaleRejectOrder(String startTime, String endTime, Kv condKv) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		where.append(" and order_status = ?");
		params.add(OrderStatusEnum.normal.getValue());
		where.append(" and audit_status = ?");
		params.add(AuditStatusEnum.pass.getValue());
		if(StringUtils.isNotEmpty(startTime)) {
			where.append(" and order_time  >= ?");
			params.add(startTime);
		}
		if(StringUtils.isNotEmpty(endTime)) {
			where.append(" and order_time  <= ?");
			params.add(endTime);
		}
		
		conditionFilter(condKv, where, params);
		
		return SaleRejectOrder.dao.findFirstByCache(CommonConstant.CACHE_NAME_FIVE_MINUTE_STORE, "sale.reject.order."+where.toString()+"."+params.toString(), "select ifnull(sum(amount),0) as amount, ifnull(sum(other_cost_amount),0) as other_cost_amount, ifnull(sum(paid_amount),0) as paid_amount from sale_reject_order "+where.toString(), params.toArray());
	}

	/**
	 * 统计进货单
	 * @return
	 */
	public PurchaseOrder sumPurchaseOrder(String startTime, String endTime, Kv condKv) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		where.append(" and order_status = ?");
		params.add(OrderStatusEnum.normal.getValue());
		where.append(" and audit_status = ?");
		params.add(AuditStatusEnum.pass.getValue());
		
		if(StringUtils.isNotEmpty(startTime)) {
			where.append(" and order_time  >= ?");
			params.add(startTime);
		}
		if(StringUtils.isNotEmpty(endTime)) {
			where.append(" and order_time  <= ?");
			params.add(endTime);
		}
		
		conditionFilter(condKv, where, params);
		
		return PurchaseOrder.dao.findFirstByCache(CommonConstant.CACHE_NAME_FIVE_MINUTE_STORE, "purchase.order."+where.toString()+"."+params.toString(), "select ifnull(sum(amount),0) as amount, ifnull(sum(other_cost_amount),0) as other_cost_amount, ifnull(sum(paid_amount),0) as paid_amount from purchase_order "+where.toString(), params.toArray());
	}
	
	/**
	 * 统计进货退货单
	 * @return
	 */
	public PurchaseRejectOrder sumPurchaseRejectOrder(String startTime, String endTime, Kv condKv) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		where.append(" and order_status = ?");
		params.add(OrderStatusEnum.normal.getValue());
		where.append(" and audit_status = ?");
		params.add(AuditStatusEnum.pass.getValue());
		
		if(StringUtils.isNotEmpty(startTime)) {
			where.append(" and order_time  >= ?");
			params.add(startTime);
		}
		if(StringUtils.isNotEmpty(endTime)) {
			where.append(" and order_time  <= ?");
			params.add(endTime);
		}
		
		conditionFilter(condKv, where, params);
		
		return PurchaseRejectOrder.dao.findFirstByCache(CommonConstant.CACHE_NAME_FIVE_MINUTE_STORE, "purchase.reject.order."+where.toString()+"."+params.toString(), "select ifnull(sum(amount),0) as amount, ifnull(sum(paid_amount),0) as paid_amount from purchase_reject_order "+where.toString(), params.toArray());
	}
	
	/**
	 * 统计盘点单
	 * @return
	 */
	public InventoryChecking sumInventoryChecking(String startTime, String endTime) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		where.append(" and order_status = ?");
		params.add(OrderStatusEnum.normal.getValue());
		where.append(" and audit_status = ?");
		params.add(AuditStatusEnum.pass.getValue());
		
		if(StringUtils.isNotEmpty(startTime)) {
			where.append(" and order_time  >= ?");
			params.add(startTime);
		}
		if(StringUtils.isNotEmpty(endTime)) {
			where.append(" and order_time  <= ?");
			params.add(endTime);
		}
		return InventoryChecking.dao.findFirstByCache(CommonConstant.CACHE_NAME_FIVE_MINUTE_STORE, "inventory.checking."+where.toString()+"."+params.toString(), "select ifnull(sum(profit_loss),0) as profit_loss from inventory_checking "+where.toString(), params.toArray());
	}
	
	/**
	 * 统计日常收支单,收入
	 * @return
	 */
	public TraderIncomeExpenses sumIncome(String startTime, String endTime, Kv condKv) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		where.append(" and order_status = ?");
		params.add(OrderStatusEnum.normal.getValue());
		where.append(" and audit_status = ?");
		params.add(AuditStatusEnum.pass.getValue());
		
		if(StringUtils.isNotEmpty(startTime)) {
			where.append(" and order_time  >= ?");
			params.add(startTime);
		}
		if(StringUtils.isNotEmpty(endTime)) {
			where.append(" and order_time  <= ?");
			params.add(endTime);
		}
		where.append(" and fund_flow = ?");
		params.add(FundFlowEnum.income.getValue());
		
		conditionFilter(condKv, where, params);
		
		return TraderIncomeExpenses.dao.findFirstByCache(CommonConstant.CACHE_NAME_FIVE_MINUTE_STORE, "trader.income.expenses."+where.toString()+"."+params.toString(), "select ifnull(sum(amount),0) as amount from trader_income_expenses "+where.toString(), params.toArray());
	}
	
	/**
	 * 统计日常收支单，支出
	 * @return
	 */
	public TraderIncomeExpenses sumExpenses(String startTime, String endTime, Kv condKv) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		where.append(" and order_status = ?");
		params.add(OrderStatusEnum.normal.getValue());
		where.append(" and audit_status = ?");
		params.add(AuditStatusEnum.pass.getValue());
		
		if(StringUtils.isNotEmpty(startTime)) {
			where.append(" and order_time  >= ?");
			params.add(startTime);
		}
		if(StringUtils.isNotEmpty(endTime)) {
			where.append(" and order_time  <= ?");
			params.add(endTime);
		}
		where.append(" and fund_flow = ?");
		params.add(FundFlowEnum.expenses.getValue());
		
		conditionFilter(condKv, where, params);
		
		return TraderIncomeExpenses.dao.findFirstByCache(CommonConstant.CACHE_NAME_FIVE_MINUTE_STORE, "trader.income.expenses."+where.toString()+"."+params.toString(), "select ifnull(sum(amount),0) as amount from trader_income_expenses "+where.toString(), params.toArray());
	}

}