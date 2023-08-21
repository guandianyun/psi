/**
 * 
 */
package com.bytechainx.psi.web.epc.event.sale;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.model.CustomerInfo;
import com.bytechainx.psi.sale.service.CustomerInfoService;
import com.bytechainx.psi.web.epc.base.BaseTraderEvent;
import com.jfinal.aop.Aop;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.silkie.epc.EpcErrCode;
import com.silkie.epc.impl.Collision;
import com.silkie.epc.impl.EpcEventParam;

/**
 * 客户管理
 * @author defier
 *
 */
public class CustomerInfoEvent extends BaseTraderEvent {

	private static final Log LOG = Log.getLog(CustomerInfoEvent.class);
	private String action; // 执行的方法名，通过这个参数判断需要执行什么行为
	private List<Object> paramList; // 参数列表，按照接口方法顺序存入

	public CustomerInfoEvent(String action) {
		this.action = action;
	}

	@Override
	protected int checkBizParam(EpcEventParam param) throws Exception {
		if (param == null) {
			if (LOG.isWarnEnabled())
				LOG.warn("未收到事件参数");
			return EpcErrCode.INVALID_PARAM;
		}
		paramList = param.getListValue(ArrayList.class.getName());
		if(paramList == null) {
			return EpcErrCode.INVALID_PARAM;
		}
		return EpcErrCode.SUCCESS;
	}

	@Override
	protected void dealBiz() throws Exception {
		CustomerInfoService customerInfoService = Aop.get(CustomerInfoService.class);
		Ret ret = Ret.ok();
		if(StringUtils.equals(action, "create")) {
			Object customerInfoObject = paramList.get(0);
			if(customerInfoObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			ret =  customerInfoService.create(getAdminId(), (CustomerInfo)customerInfoObject);
			
		} else if(StringUtils.equals(action, "update")) {
			Object customerInfoObject = paramList.get(0);
			if(customerInfoObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			ret =  customerInfoService.update(getAdminId(), (CustomerInfo)customerInfoObject);
		} else if(StringUtils.equals(action, "updateOpenBalance")) {
			Object customerInfoIdObject = paramList.get(0);
			if(customerInfoIdObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			Object openBalanceObject = paramList.get(1);
			if(openBalanceObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			ret =  customerInfoService.updateOpenBalance(getAdminId(), (Integer)customerInfoIdObject, (BigDecimal)openBalanceObject);
		}
		responseMsg(ret);
	}


	@Override
	public Collision getCollision() {
		return Collision.generateCollision("tenant.org.id.0");
	}


}
