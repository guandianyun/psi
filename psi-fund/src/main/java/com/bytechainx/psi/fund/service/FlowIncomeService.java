package com.bytechainx.psi.fund.service;


import java.util.List;

import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.model.TraderIncomeExpenses;
import com.bytechainx.psi.common.model.TraderIncomeExpensesFund;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 收入单据
*/
public class FlowIncomeService extends CommonService {

	@Inject
	private FlowExpensesService expensesService;

	/**
	* 分页列表
	*/
	public Page<TraderIncomeExpenses> paginate(String startDay, String endDay, Kv conditionColumns, Integer pageNumber, int pageSize) {
		return expensesService.paginate(startDay, endDay, conditionColumns, pageNumber, pageSize);
	}


	/**
	* 新增
	*/
	public Ret create(TraderIncomeExpenses order, List<TraderIncomeExpensesFund> orderRefList) {
		return expensesService.create(order, orderRefList);
	}


	/**
	* 修改
	*/
	public Ret update(TraderIncomeExpenses order, List<TraderIncomeExpensesFund> orderRefList) {
		return expensesService.update(order, orderRefList);
	}


	/**
	* 审核
	*/
	public Ret audit(List<Integer> ids, AuditStatusEnum auditStatus, String auditDesc, Integer auditorId) {
		return expensesService.audit(ids, auditStatus, auditDesc, auditorId);
	}


	/**
	* 作废
	*/
	public Ret disable(List<Integer> ids) {
		return expensesService.disable(ids);
	}
	
	public Ret delete(Integer id) {
		return expensesService.delete(id);
	}
	
	/**
	 * 导出数据xls
	 */
	public Ret export(Integer handlerId, String startDay, String endDay, Kv condKv) {
		return expensesService.export(handlerId, startDay, endDay, condKv);
	}

}