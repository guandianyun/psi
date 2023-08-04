package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.model.base.BaseTraderIncomeExpensesFund;

/**
 * 日常收支明细
 */
@SuppressWarnings("serial")
public class TraderIncomeExpensesFund extends BaseTraderIncomeExpensesFund<TraderIncomeExpensesFund> {
	
	public static final TraderIncomeExpensesFund dao = new TraderIncomeExpensesFund().dao();
	
	public TraderBalanceAccount getBalanceAccount() {
		return TraderBalanceAccount.dao.findById(getTraderBalanceAccountId());
	}
}

