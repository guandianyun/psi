package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.model.base.BasePurchaseRejectOrderFee;

/**
 * 进货退货单
 */
@SuppressWarnings("serial")
public class PurchaseRejectOrderFee extends BasePurchaseRejectOrderFee<PurchaseRejectOrderFee> {
	
	public static final PurchaseRejectOrderFee dao = new PurchaseRejectOrderFee().dao();
	
	/**
	 * 项目开支类型
	 * @return
	 */
	public TraderFundType getFundType() {
		return TraderFundType.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "trader.fund.type.id."+getTraderFundType(), "select * from trader_fund_type where id = ?", getTraderFundType());
	}
	
	public PurchaseRejectOrderFee findByFundType(Integer orderId, Integer fundType) {
		return PurchaseRejectOrderFee.dao.findFirst("select * from purchase_reject_order_fee where purchase_reject_order_id = ? and trader_fund_type = ?", orderId, fundType);
	}
	
}

