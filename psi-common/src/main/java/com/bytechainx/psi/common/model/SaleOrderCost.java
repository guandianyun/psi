package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.model.base.BaseSaleOrderCost;

/**
 * 销售单成本支出
 */
@SuppressWarnings("serial")
public class SaleOrderCost extends BaseSaleOrderCost<SaleOrderCost> {
	
	public static final SaleOrderCost dao = new SaleOrderCost().dao();
	
	
	/**
	 * 项目开支类型
	 * @return
	 */
	public TraderFundType getFundType() {
		return TraderFundType.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "trader.fund.type.id."+getTraderFundType(), "select * from trader_fund_type where id = ?", getTraderFundType());
	}
}

