package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.model.base.BaseTenantPayOrderLog;

/**
 * 支付日志
 */
@SuppressWarnings("serial")
public class TenantPayOrderLog extends BaseTenantPayOrderLog<TenantPayOrderLog> {
	
	public static final TenantPayOrderLog dao = new TenantPayOrderLog().dao();
	
	
	public TenantPayOrderLog findByCode(String orderCode) {
		return TenantPayOrderLog.dao.findFirst("select * from tenant_pay_order_log where order_code = ?", orderCode);
	}
}

