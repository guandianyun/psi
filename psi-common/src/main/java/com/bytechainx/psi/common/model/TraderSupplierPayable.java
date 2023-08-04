package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.EnumConstant.CheckingRefOrderTypeEnum;
import com.bytechainx.psi.common.model.base.BaseTraderSupplierPayable;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * 供应商应付对账
 */
@SuppressWarnings("serial")
public class TraderSupplierPayable extends BaseTraderSupplierPayable<TraderSupplierPayable> {
	
	public static final TraderSupplierPayable dao = new TraderSupplierPayable().dao();
	
	public SupplierInfo getSupplierInfo() {
		return SupplierInfo.dao.findById(getSupplierInfoId());
	}
	
	public TraderBookAccount getTraderBookAccount() {
		return TraderBookAccount.dao.findById(getTraderBookAccountId());
	}
	
	public TraderSupplierPayable findByOrderId(CheckingRefOrderTypeEnum checkingRefOrderType, Integer orderId) {
		return TraderSupplierPayable.dao.findFirst("select * from trader_supplier_payable where ref_order_type = ? and ref_order_id = ? limit 1", checkingRefOrderType.getValue(), orderId);
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

