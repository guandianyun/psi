package com.bytechainx.psi.web.web.controller.fund;


import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.TraderFundOrder;
import com.bytechainx.psi.fund.service.StatFundOrderService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Page;


/**
* 资金流水
*/
@Path("/fund/stat/fundOrder")
public class StatFundOrderController extends BaseController {

	@Inject
	private StatFundOrderService statFundOrderService;

	/**
	* 首页
	*/
	@Permission(Permissions.fund_stat_fundOrder)
	public void index() {

	}
	
	/**
	* 列表
	*/
	@Permission(Permissions.fund_stat_fundOrder)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		pageSize = getPageSize();
		
		Kv condKv = Kv.create();
		
		
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		Page<TraderFundOrder> page = statFundOrderService.paginate(startTime, endTime, condKv, pageNumber, pageSize);
		
		condKv.set("fund_flow", FundFlowEnum.income.getValue());
		TraderFundOrder sumIncomeAmount = statFundOrderService.sumAmount(startTime, endTime, condKv);
		
		condKv.set("fund_flow", FundFlowEnum.expenses.getValue());
		TraderFundOrder sumExpensesAmount = statFundOrderService.sumAmount(startTime, endTime, condKv);
		
		setAttr("page", page);
		setAttr("sumExpensesAmount", sumExpensesAmount.getAmount());
		setAttr("sumIncomeAmount", sumIncomeAmount.getAmount());
	}

	
}