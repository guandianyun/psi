package com.bytechainx.psi.web.web.controller.fund;


import java.math.BigDecimal;

import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.InventoryChecking;
import com.bytechainx.psi.common.model.PurchaseOrder;
import com.bytechainx.psi.common.model.PurchaseRejectOrder;
import com.bytechainx.psi.common.model.SaleOrder;
import com.bytechainx.psi.common.model.SaleRejectOrder;
import com.bytechainx.psi.common.model.TraderIncomeExpenses;
import com.bytechainx.psi.fund.service.StatProfitService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;


/**
* 经营利润
*/
@Path("/fund/stat/profit")
public class StatProfitController extends BaseController {

	@Inject
	private StatProfitService statProfitService;

	/**
	* 首页
	*/
	@Permission(Permissions.fund_stat_profit)
	public void index() {

	}
	
	/**
	* 列表
	*/
	@Permission(Permissions.fund_stat_profit)
	public void list() {
		Kv condKv = Kv.create();
		conditionFilterStore(condKv, null); // 添加门店过滤条件
		String startTime = get("start_time");
		String endTime = get("end_time");
		
		SaleOrder sumSaleOrder = statProfitService.sumSaleOrder(startTime, endTime, condKv);
		SaleRejectOrder sumSaleRejectOrder = statProfitService.sumSaleRejectOrder(startTime, endTime, condKv);
		InventoryChecking sumInventoryChecking = statProfitService.sumInventoryChecking(startTime, endTime);
		PurchaseRejectOrder sumPurchaseRejectOrder = statProfitService.sumPurchaseRejectOrder(startTime, endTime, condKv);
		TraderIncomeExpenses sumIncome = statProfitService.sumIncome(startTime, endTime, condKv);
		PurchaseOrder sumPurchaseOrder = statProfitService.sumPurchaseOrder(startTime, endTime, condKv);
		TraderIncomeExpenses sumExpenses = statProfitService.sumExpenses(startTime, endTime, condKv);

		
		setAttr("saleAmount", sumSaleOrder.getAmount()); // 销售收入
		setAttr("saleAmountRemain", sumSaleOrder.getAmount().subtract(sumSaleOrder.getPaidAmount())); // 销售剩余应收
		setAttr("saleCostAmount", sumSaleOrder.getOtherCostAmount()); // 销售成本
		BigDecimal profitLoss = sumInventoryChecking.getProfitLoss();
		if(profitLoss.compareTo(BigDecimal.ZERO) > 0) {
			setAttr("checkingProfitAmount", profitLoss); // 盘盈
			setAttr("checkingLossAmount", BigDecimal.ZERO); // 盘亏
		} else {
			setAttr("checkingProfitAmount", BigDecimal.ZERO);
			setAttr("checkingLossAmount", profitLoss);
		}
		setAttr("purchaseRejectAmount", sumPurchaseRejectOrder.getAmount()); // 进货退货收入
		setAttr("purchaseRejectAmountRemain", sumPurchaseRejectOrder.getAmount().subtract(sumPurchaseRejectOrder.getPaidAmount())); // 进货退货剩余应收
		setAttr("incomeAmount", sumIncome.getAmount()); // 日常收入
		setAttr("purchaseAmount", sumPurchaseOrder.getAmount().add(sumPurchaseOrder.getOtherCostAmount())); // 进货成本支出
		setAttr("purchaseAmountRemain", sumPurchaseOrder.getAmount().subtract(sumPurchaseOrder.getPaidAmount())); // 进货成本剩余应付
		setAttr("saleRejectAmount", sumSaleRejectOrder.getAmount().add(sumSaleRejectOrder.getOtherCostAmount())); // 销售退货支出
		setAttr("saleRejectAmountRemain", sumSaleRejectOrder.getAmount().subtract(sumSaleRejectOrder.getPaidAmount())); // 销售退货剩余应付
		setAttr("expensesAmount", sumExpenses.getAmount()); // 日常支出
	}
	
	
}