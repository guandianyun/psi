package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.EnumConstant.FundTypeEnum;
import com.bytechainx.psi.common.model.base.BasePurchaseOrderFund;

/**
 * 进货单资金明细
 */
@SuppressWarnings("serial")
public class PurchaseOrderFund extends BasePurchaseOrderFund<PurchaseOrderFund> {
	
	public static final PurchaseOrderFund dao = new PurchaseOrderFund().dao();
	
	public TraderBalanceAccount getBalanceAccount() {
		if(getPayType() != FundTypeEnum.cash.getValue()) {
			return null;
		}
		return TraderBalanceAccount.dao.findById(getBalanceAccountId());
	}
	
	public String getAccountName() {
		if(getPayType() == FundTypeEnum.cash.getValue()) {
			return getBalanceAccount().getName();
		} else if(getPayType() == FundTypeEnum.balance.getValue()) {
			return "供应商账户";
		} else if(getPayType() == FundTypeEnum.checking.getValue()) {
			return FundTypeEnum.checking.getName();
		}
		return null;
	}
}

