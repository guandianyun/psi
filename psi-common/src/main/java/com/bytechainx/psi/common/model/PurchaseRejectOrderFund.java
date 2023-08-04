package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.EnumConstant.FundTypeEnum;
import com.bytechainx.psi.common.model.base.BasePurchaseRejectOrderFund;

/**
 * 进货退货单资金明细
 */
@SuppressWarnings("serial")
public class PurchaseRejectOrderFund extends BasePurchaseRejectOrderFund<PurchaseRejectOrderFund> {

	public static final PurchaseRejectOrderFund dao = new PurchaseRejectOrderFund().dao();
	
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

