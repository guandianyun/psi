package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.RefOrderTypeEnum;
import com.bytechainx.psi.common.model.base.BaseTraderFundOrder;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * 资金流水
 */
@SuppressWarnings("serial")
public class TraderFundOrder extends BaseTraderFundOrder<TraderFundOrder> {
	
	public static final TraderFundOrder dao = new TraderFundOrder().dao();
	
	public TraderFundOrder findById(Integer id) {
		return TraderFundOrder.dao.findFirst("select * from trader_fund_order where id = ?", id);
	}
	
	/**
	 * 查询某个订单交易流水
	 * @return
	 */
	public TraderFundOrder findByOrderId(Integer balanceAccountId, RefOrderTypeEnum refOrderType, Integer orderId) {
		return TraderFundOrder.dao.findFirst("select * from trader_fund_order where trader_balance_account_id = ? and ref_order_type = ? and ref_order_id = ? limit 1", balanceAccountId, refOrderType.getValue(), orderId);
	}
	
	
	/**
	 * 结算账户
	 * @return
	 */
	public TraderBalanceAccount getTraderBalanceAccount() {
		return TraderBalanceAccount.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "trader.balance.account."+getTraderBalanceAccountId(), "select * from trader_balance_account where id = ?", getTraderBalanceAccountId());
	}
	
	public String getRefOrderTypeName() {
		return RefOrderTypeEnum.getEnum(getRefOrderType()).getName();
	}
	
	public Record getOrder() {
		RefOrderTypeEnum refOrderType = RefOrderTypeEnum.getEnum(getRefOrderType());
		if(refOrderType.getValue() == RefOrderTypeEnum.balance_account.getValue()) {
			return null;
		}
		Record order = Db.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "record."+refOrderType.name().replaceAll("_", ".")+".id."+getRefOrderId(), "select * from "+refOrderType.name()+" where id = ?", getRefOrderId());
		if(refOrderType.getValue() == RefOrderTypeEnum.sale_book_order.getValue() || refOrderType.getValue() == RefOrderTypeEnum.sale_order.getValue()
				|| refOrderType.getValue() == RefOrderTypeEnum.sale_reject_order.getValue() || refOrderType.getValue() == RefOrderTypeEnum.trader_receipt_order.getValue()) { // 销售类
			CustomerInfo customerInfo = CustomerInfo.dao.findById(order.getInt("customer_info_id"));
			order.put("handler", customerInfo);
		} else if(refOrderType.getValue() == RefOrderTypeEnum.purchase_book_order.getValue() || refOrderType.getValue() == RefOrderTypeEnum.purchase_order.getValue()
				|| refOrderType.getValue() == RefOrderTypeEnum.purchase_reject_order.getValue() || refOrderType.getValue() == RefOrderTypeEnum.trader_pay_order.getValue()) {
			SupplierInfo supplierInfo = SupplierInfo.dao.findById(order.getInt("supplier_info_id"));
			order.put("handler", supplierInfo);
		} else {
			
		}
		
		return order;
	}

}


