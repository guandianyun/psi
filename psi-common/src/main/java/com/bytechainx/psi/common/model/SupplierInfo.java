package com.bytechainx.psi.common.model;

import java.math.BigDecimal;
import java.util.Date;

import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.model.base.BaseSupplierInfo;

/**
 * 供应商
 */
@SuppressWarnings("serial")
public class SupplierInfo extends BaseSupplierInfo<SupplierInfo> {
	
	public static final SupplierInfo dao = new SupplierInfo().dao();
	
	public TraderBookAccount getTraderBookAccount() {
		return TraderBookAccount.dao.findById(getTraderBookAccountId());
	}
	
	/**
	 * 欠供应商款
	 * @return
	 */
	public BigDecimal getDebtAmount() {
		TraderBookAccount traderBookAccount = getTraderBookAccount();
		return traderBookAccount.getSupplierDebtAmount();
	}
	
	/**
	 * 供应商可用平账资金
	 * @return
	 */
	public BigDecimal getAvailableAmount() {
		TraderBookAccount traderBookAccount = getTraderBookAccount();
		return traderBookAccount.getSupplierAvailableAmount();
	}
	
	
	public SupplierCategory getSupplierCategory() {
		return SupplierCategory.dao.findById(getSupplierCategoryId());
	}
	
	
	public SupplierInfo findById(Integer id) {
		return SupplierInfo.dao.findFirst("select * from supplier_info where id = ? limit 1", id);
	}
	
	/**
	 * 供应商进货单的开单数、进货额统计
	 * @return
	 */
	public PurchaseOrder getPurchaseStat() {
		return PurchaseOrder.dao.findFirst("select count(*) as orderCount, sum(amount) as sumAmount from purchase_order where supplier_info_id = ? and order_status = ? and audit_status = ?", getId(), OrderStatusEnum.normal.getValue(), AuditStatusEnum.pass.getValue());
	}
	
	/**
	 * 最近开单
	 * @return
	 */
	public Integer getLastOrderDays() {
		if(getLastOrderTime() == null) {
			return -1;
		}
		return DateUtil.getDaySpaceBetween(getLastOrderTime(), new Date());
	}
}

