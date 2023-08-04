package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.kit.OrderCodeBuilder;
import com.bytechainx.psi.common.model.base.BaseTraderTransferOrder;
import com.jfinal.plugin.redis.Redis;

/**
 * 结算账户互转单
 */
@SuppressWarnings("serial")
public class TraderTransferOrder extends BaseTraderTransferOrder<TraderTransferOrder> {
	
	public static final TraderTransferOrder dao = new TraderTransferOrder().dao();
	
	public TraderTransferOrder findById(Integer id) {
		return TraderTransferOrder.dao.findFirst("select * from trader_transfer_order where id = ? limit 1", id);
	}
	
	// 单据编号缓存redis key
	private static final String REDIS_ORDER_CODE_LIST_KEY = "order.code.trader.transfer.order.tenant.id.";

	/**
	 * 生成单据编号，从redis队列中获取
	 * 
	 * @return
	 */
	public String generateOrderCode() {
		String code = Redis.use().lpop(REDIS_ORDER_CODE_LIST_KEY );
		if(code == null) {
			return null;
		}
		return "HZD"+code;
	}

	/**
	 * 预生成单据编号，存入redis队列
	 * 
	 * @param tenantOrgId
	 */
	public void buildOrderCodes() {
		OrderCodeBuilder.build(REDIS_ORDER_CODE_LIST_KEY, "trader_transfer_order");
	}

	/**
	 * 获取转出帐户
	 * @return
	 */
	public TraderBalanceAccount getOutBalanceAccount() {
		return TraderBalanceAccount.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "trader.balance.account.id."+getOutAccountId(), "select * from trader_balance_account where id = ?", getOutAccountId());
	}
	/**
	 * 获取转入帐户
	 * @return
	 */
	public TraderBalanceAccount getInBalanceAccount() {
		return TraderBalanceAccount.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "trader.balance.account.id."+getInAccountId(), "select * from trader_balance_account where id = ?", getInAccountId());
	}
	
	/**
	 * 经办人
	 * @return
	 */
	public TenantAdmin getHandler() {
		return TenantAdmin.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "tenant.admin.id."+getHandlerId(), "select * from tenant_admin where id = ?", getHandlerId());
	}
	/**
	 * 制单人
	 * @return
	 */
	public TenantAdmin getMakeMan() {
		return TenantAdmin.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "tenant.admin.id."+getMakeManId(), "select * from tenant_admin where id = ?", getMakeManId());
	}
	/**
	 * 审核人
	 * @return
	 */
	public TenantAdmin getAuditor() {
		return TenantAdmin.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "tenant.admin.id."+getAuditorId(), "select * from tenant_admin where id = ?", getAuditorId());
	}
	
	
	/**
	 * 审核设置，单据是否需要审核
	 * @return
	 */
	public boolean getAuditConfigFlag() {
		TenantConfig auditConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.audit_trader_transfer_order);
		return Boolean.parseBoolean(auditConfig.getAttrValue());
	}
}

