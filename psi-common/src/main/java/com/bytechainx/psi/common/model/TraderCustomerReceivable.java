package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.CheckingRefOrderTypeEnum;
import com.bytechainx.psi.common.model.base.BaseTraderCustomerReceivable;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * 客户应收对账
 */
@SuppressWarnings("serial")
public class TraderCustomerReceivable extends BaseTraderCustomerReceivable<TraderCustomerReceivable> {
	
	public static final TraderCustomerReceivable dao = new TraderCustomerReceivable().dao();
	
	public CustomerInfo getCustomerInfo() {
		return CustomerInfo.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "customer.info.id."+getCustomerInfoId(), "select * from customer_info where id = ?", getCustomerInfoId());
	}
	
	public TraderBookAccount getTraderBookAccount() {
		return TraderBookAccount.dao.findById(getTraderBookAccountId());
	}
	
	public TraderCustomerReceivable findByOrderId(CheckingRefOrderTypeEnum checkingRefOrderType, Integer orderId) {
		return TraderCustomerReceivable.dao.findFirst("select * from trader_customer_receivable where ref_order_type = ? and ref_order_id = ? limit 1", checkingRefOrderType.getValue(), orderId);
	}
	
	
	public String getRefOrderTypeName() {
		return CheckingRefOrderTypeEnum.getEnum(getRefOrderType()).getName();
	}
	
	/**
	 * 关联单
	 * @return
	 */
	public Record getRefOrder() {
		CheckingRefOrderTypeEnum refOrderType = CheckingRefOrderTypeEnum.getEnum(getRefOrderType());
		return Db.findFirst("select * from "+refOrderType.name()+" where id = "+getRefOrderId());
	}
	
}

