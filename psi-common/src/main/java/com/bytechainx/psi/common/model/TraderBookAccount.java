package com.bytechainx.psi.common.model;

import java.math.BigDecimal;

import com.bytechainx.psi.common.model.base.BaseTraderBookAccount;

/**
 * 往来账户
 */
@SuppressWarnings("serial")
public class TraderBookAccount extends BaseTraderBookAccount<TraderBookAccount> {
	
	public static final TraderBookAccount dao = new TraderBookAccount().dao();
	
	
	/**
	 * 客户欠款,支出-收入
	 * @return
	 */
	public BigDecimal getCustomerDebtAmount() {
		return getOutAmount().subtract(getInAmount());
	}
	/**
	 * 客户可用支付余额，用于余额支付，总收入-平账支出
	 * @return
	 */
	public BigDecimal getCustomerAvailableAmount() {
		return getInAmount().subtract(getPayAmount());
	}
	
	/**
	 * 欠供应商款,收入-支出
	 * @return
	 */
	public BigDecimal getSupplierDebtAmount() {
		return getInAmount().subtract(getOutAmount());
	}
	
	/**
	 * 供应商可用支付余额，用于余额支付，总支出-平账支出
	 * @return
	 */
	public BigDecimal getSupplierAvailableAmount() {
		return getOutAmount().subtract(getPayAmount());
	}
	
}

