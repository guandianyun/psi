package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.model.base.BasePurchaseOrderCost;

/**
 * 进货单成本支出
 */
@SuppressWarnings("serial")
public class PurchaseOrderCost extends BasePurchaseOrderCost<PurchaseOrderCost> {
	
	public static final PurchaseOrderCost dao = new PurchaseOrderCost().dao();
	
	/**
	 * 项目开支类型
	 * @return
	 */
	public TraderFundType getFundType() {
		return TraderFundType.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "trader.fund.type.id."+getTraderFundType(), "select * from trader_fund_type where id = ?", getTraderFundType());
	}
	
}

