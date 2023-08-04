package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.model.base.BaseSaleOrderFee;

/**
 * 销售单其他费用，需要客户支付
 */
@SuppressWarnings("serial")
public class SaleOrderFee extends BaseSaleOrderFee<SaleOrderFee> {
	public static final SaleOrderFee dao = new SaleOrderFee().dao();
	
	/**
	 * 项目开支类型
	 * @return
	 */
	public TraderFundType getFundType() {
		return TraderFundType.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "trader.fund.type.id."+getTraderFundType(), "select * from trader_fund_type where id = ?", getTraderFundType());
	}
	
	public SaleOrderFee findByFundType(Integer orderId, Integer fundType) {
		return SaleOrderFee.dao.findFirst("select * from sale_order_fee where sale_order_id = ? and trader_fund_type = ?", orderId, fundType);
	}
	
}

