package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.FundTypeEnum;
import com.bytechainx.psi.common.model.base.BaseSaleOrderFund;

/**
 * 销售单资金明细
 */
@SuppressWarnings("serial")
public class SaleOrderFund extends BaseSaleOrderFund<SaleOrderFund> {
	
	public static final SaleOrderFund dao = new SaleOrderFund().dao();
	
	public TraderBalanceAccount getBalanceAccount() {
		if(getReceiptType() != FundTypeEnum.cash.getValue()) {
			return null;
		}
		return TraderBalanceAccount.dao.findById(getBalanceAccountId());
	}
	
	public String getAccountName() {
		if(getReceiptType() == FundTypeEnum.cash.getValue()) {
			return getBalanceAccount().getName();
		} else if(getReceiptType() == FundTypeEnum.balance.getValue()) {
			return "客户账户";
		} else if(getReceiptType() == FundTypeEnum.checking.getValue()) {
			return FundTypeEnum.checking.getName();
		}
		return null;
	}
	
	public SaleOrder getSaleOrder() {
		return SaleOrder.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "sale.order.id."+getSaleOrderId(), "select * from sale_order where id = ?", getSaleOrderId());
	}
}

