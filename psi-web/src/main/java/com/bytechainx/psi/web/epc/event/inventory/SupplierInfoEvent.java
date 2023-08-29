/**
 * 
 */
package com.bytechainx.psi.web.epc.event.inventory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.model.SupplierInfo;
import com.bytechainx.psi.purchase.service.SupplierInfoService;
import com.bytechainx.psi.web.epc.base.BaseTraderEvent;
import com.jfinal.aop.Aop;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.silkie.epc.EpcErrCode;
import com.silkie.epc.impl.Collision;
import com.silkie.epc.impl.EpcEventParam;

/**
 * 供应商管理
 * @author defier
 *
 */
public class SupplierInfoEvent extends BaseTraderEvent {

	private static final Log LOG = Log.getLog(SupplierInfoEvent.class);
	private String action; // 执行的方法名，通过这个参数判断需要执行什么行为
	private List<Object> paramList; // 参数列表，按照接口方法顺序存入

	public SupplierInfoEvent(String action) {
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
		SupplierInfoService supplierInfoService = Aop.get(SupplierInfoService.class);
		Ret ret = Ret.ok();
		if(StringUtils.equals(action, "create")) {
			Object supplierInfoObject = paramList.get(0);
			if(supplierInfoObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			ret =  supplierInfoService.create(getAdminId(), (SupplierInfo)supplierInfoObject);
			
		} else if(StringUtils.equals(action, "update")) {
			Object supplierInfoObject = paramList.get(0);
			if(supplierInfoObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			ret =  supplierInfoService.update(getAdminId(), (SupplierInfo)supplierInfoObject);
			
		} else if(StringUtils.equals(action, "updateOpenBalance")) {
			Object supplierInfoIdObject = paramList.get(0);
			if(supplierInfoIdObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			Object openBalanceObject = paramList.get(1);
			if(openBalanceObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			ret =  supplierInfoService.updateOpenBalance(getAdminId(), (Integer)supplierInfoIdObject, (BigDecimal)openBalanceObject);
		}
		responseMsg(ret);
	}


	@Override
	public Collision getCollision() {
		return Collision.generateCollision("tenant.org.id.0");
	}


}
