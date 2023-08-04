package com.bytechainx.psi.common.model;

import java.math.BigDecimal;
import java.util.Date;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FlagEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.SalePayTypeEnum;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.model.base.BaseCustomerInfo;

/**
 * 客户信息
 */
@SuppressWarnings("serial")
public class CustomerInfo extends BaseCustomerInfo<CustomerInfo> {
	
	public static final CustomerInfo dao = new CustomerInfo().dao();
	/**
	 * 客户往来账户
	 * @return
	 */
	public TraderBookAccount getTraderBookAccount() {
		return TraderBookAccount.dao.findById(getTraderBookAccountId());
	}
	
	/**
	 * 客户欠款
	 * @return
	 */
	public BigDecimal getDebtAmount() {
		TraderBookAccount traderBookAccount = getTraderBookAccount();
		return traderBookAccount.getCustomerDebtAmount();
	}
	/**
	 * 客户可用平账资金
	 * @return
	 */
	public BigDecimal getAvailableAmount() {
		TraderBookAccount traderBookAccount = getTraderBookAccount();
		return traderBookAccount.getCustomerAvailableAmount();
	}
	
	public CustomerCategory getCustomerCategory() {
		return CustomerCategory.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "customer.category.id."+getCustomerCategoryId(), "select * from customer_category where id = ?", getCustomerCategoryId());
	}
	
	public CustomerPriceLevel getCustomerPriceLevel() {
		return CustomerPriceLevel.dao.findById(getCustomerPriceLevelId());
	}
	
	
	public CustomerInfo findByMobile(String mobile) {
		return CustomerInfo.dao.findFirst("select * from customer_info where mobile = ? or moble_1 = ? or mobile_2 = ? limit 1", mobile, mobile, mobile);
	}
	
	/**
	 * 客户销售单的开单数、销售额、销售成本统计
	 * @return
	 */
	public SaleOrder getSaleStat() {
		return SaleOrder.dao.findFirst("select count(*) as orderCount, sum(amount) as sumAmount, sum(goods_cost_amount) as sumCostAmount from sale_order where customer_info_id = ? and order_status = ? and audit_status = ?", getId(), OrderStatusEnum.normal.getValue(), AuditStatusEnum.pass.getValue());
	}
	/**
	 * 业务员
	 * @return
	 */
	public TenantAdmin getHandler() {
		return TenantAdmin.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "tenant.admin.id."+getHandlerId(), "select * from tenant_admin where id = ?", getHandlerId());
	}
	
	public String getPayTypeName() {
		if(getPayType() == null) {
			return null;
		}
		return SalePayTypeEnum.getEnum(getPayType()).getName();
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
	
	public CustomerInfo findRetailCustomer() {
		return CustomerInfo.dao.findFirst("select * from customer_info where default_flag = ? and name = '零售客户'", FlagEnum.YES.getValue());
	}
}

