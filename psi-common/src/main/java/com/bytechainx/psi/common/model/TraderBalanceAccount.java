package com.bytechainx.psi.common.model;

import java.math.BigDecimal;
import java.util.Date;

import com.bytechainx.psi.common.model.base.BaseTraderBalanceAccount;
import com.jfinal.plugin.activerecord.Db;

/**
 * 结算帐户
 */
@SuppressWarnings("serial")
public class TraderBalanceAccount extends BaseTraderBalanceAccount<TraderBalanceAccount> {
	
	public static final TraderBalanceAccount dao = new TraderBalanceAccount().dao();
	
	public TraderBalanceAccount findById(Integer id) {
		return TraderBalanceAccount.dao.findFirst("select * from trader_balance_account where id = ? limit 1", id);
	}
	
	public void updateBalance() {
		BigDecimal balance = Db.queryBigDecimal("select sum(balance) as sumBalance from trader_balance_store_ref where trader_balance_account_id = ?", getId());
		setBalance(balance);
		setUpdatedAt(new Date());
		update();
	}
	
	public TraderBalanceAccount findCashAccount() {
		return TraderBalanceAccount.dao.findFirst("select * from trader_balance_account where name = '现金' limit 1");
	}

	public TraderBalanceAccount findOnlinePayAccount() {
		return TraderBalanceAccount.dao.findFirst("select * from trader_balance_account where name = '在线支付' limit 1");
	}
	
}

