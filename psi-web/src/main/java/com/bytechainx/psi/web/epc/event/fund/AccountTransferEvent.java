
package com.bytechainx.psi.web.epc.event.fund;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.model.TraderTransferOrder;
import com.bytechainx.psi.fund.service.AccountTransferService;
import com.bytechainx.psi.web.epc.base.BaseTraderEvent;
import com.jfinal.aop.Aop;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.silkie.epc.EpcErrCode;
import com.silkie.epc.impl.Collision;
import com.silkie.epc.impl.EpcEventParam;

/**
 * 结算账户互转
 * 
 * @author defier
 *
 */
public class AccountTransferEvent extends BaseTraderEvent {

	private static final Log LOG = Log.getLog(AccountTransferEvent.class);
	private String action; // 执行的方法名，通过这个参数判断需要执行什么行为
	private List<Object> paramList; // 参数列表，按照接口方法顺序存入

	public AccountTransferEvent(String action) {
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

	@SuppressWarnings("unchecked")
	@Override
	protected void dealBiz() throws Exception {
		AccountTransferService transferService = Aop.get(AccountTransferService.class);
		Ret ret = Ret.ok();
		if(StringUtils.equals(action, "create")) {
			Object transferOrderObject = paramList.get(0);
			if(transferOrderObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			ret = transferService.create((TraderTransferOrder) transferOrderObject);
			
		} else if(StringUtils.equals(action, "update")) {
			Object transferOrderObject = paramList.get(0);
			if(transferOrderObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			ret = transferService.update((TraderTransferOrder) transferOrderObject);
			
		} else if(StringUtils.equals(action, "disable")) {
			Object idsObject = paramList.get(0);
			if(idsObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			ret = transferService.disable((List<Integer>) idsObject);
		}
		responseMsg(ret);
	}


	@Override
	public Collision getCollision() {
		return Collision.generateCollision("tenant.org.id.0");
	}

}
