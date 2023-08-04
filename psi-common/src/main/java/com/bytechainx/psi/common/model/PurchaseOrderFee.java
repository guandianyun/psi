package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.model.base.BasePurchaseOrderFee;

/**
 * 进货单其他费用
 */
@SuppressWarnings("serial")
public class PurchaseOrderFee extends BasePurchaseOrderFee<PurchaseOrderFee> {
	
	public static final PurchaseOrderFee dao = new PurchaseOrderFee().dao();
	
	/**
	 * 项目开支类型
	 * @return
	 */
	public TraderFundType getFundType() {
		return TraderFundType.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "trader.fund.type.id."+getTraderFundType(), "select * from trader_fund_type where id = ?", getTraderFundType());
	}
	
	public PurchaseOrderFee findByFundType(Integer orderId, Integer fundType) {
		return PurchaseOrderFee.dao.findFirst("select * from purchase_order_fee where purchase_order_id = ? and trader_fund_type = ?", orderId, fundType);
	}
	
}

