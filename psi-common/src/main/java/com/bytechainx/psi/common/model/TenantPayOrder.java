package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.EnumConstant.OnlinePayStatusEnum;
import com.bytechainx.psi.common.model.base.BaseTenantPayOrder;

/**
 * 租户支付单
 */
@SuppressWarnings("serial")
public class TenantPayOrder extends BaseTenantPayOrder<TenantPayOrder> {
	
	public static final TenantPayOrder dao = new TenantPayOrder().dao();

	/**
	 * 保存日志
	 */
	public void saveLog() {
		TenantPayOrderLog log = new TenantPayOrderLog();
		log._setOrPut(_getAttrs());
		if(log.getPayStatus() == OnlinePayStatusEnum.wait.getValue() || log.getPayStatus() == OnlinePayStatusEnum.nopay.getValue()) {
			log.setErrorMsg("支付超时");
			log.setPayStatus(OnlinePayStatusEnum.reject.getValue());
		}
		log.save();
		
		delete();
	}

	public TenantPayOrder findByCode(String orderCode) {
		return TenantPayOrder.dao.findFirst("select * from tenant_pay_order where order_code = ?", orderCode);
	}
	
}

