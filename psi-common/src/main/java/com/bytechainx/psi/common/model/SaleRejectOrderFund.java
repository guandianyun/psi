package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.EnumConstant.FundTypeEnum;
import com.bytechainx.psi.common.model.base.BaseSaleRejectOrderFund;

/**
 * 销售退货单资金明细
 */
@SuppressWarnings("serial")
public class SaleRejectOrderFund extends BaseSaleRejectOrderFund<SaleRejectOrderFund> {
	
	public static final SaleRejectOrderFund dao = new SaleRejectOrderFund().dao();
	
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
	
}

