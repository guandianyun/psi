/**
 * 
 */
package com.bytechainx.psi.web.job;

import org.quartz.JobExecutionContext;

import com.bytechainx.psi.common.model.InventoryChecking;
import com.bytechainx.psi.common.model.PurchaseOrder;
import com.bytechainx.psi.common.model.PurchaseRejectOrder;
import com.bytechainx.psi.common.model.SaleOrder;
import com.bytechainx.psi.common.model.SaleRejectOrder;
import com.bytechainx.psi.common.model.TraderIncomeExpenses;
import com.bytechainx.psi.common.model.TraderPayOrder;
import com.bytechainx.psi.common.model.TraderReceiptOrder;
import com.bytechainx.psi.common.model.TraderTransferOrder;
import com.bytechainx.psi.web.job.base.BaseJob;

/**
 * 单据编号生成任务
 * @author defier
 *
 */
public class OrderCodeBuilderJob extends BaseJob {

	@Override
	protected void run(JobExecutionContext context) {
		
		InventoryChecking.dao.buildOrderCodes();
		PurchaseOrder.dao.buildOrderCodes();
		PurchaseRejectOrder.dao.buildOrderCodes();
		SaleOrder.dao.buildOrderCodes();
		SaleRejectOrder.dao.buildOrderCodes();
		TraderPayOrder.dao.buildOrderCodes();
		TraderReceiptOrder.dao.buildOrderCodes();
		TraderTransferOrder.dao.buildOrderCodes();
		TraderIncomeExpenses.dao.buildOrderCodes();
		
	}

}
