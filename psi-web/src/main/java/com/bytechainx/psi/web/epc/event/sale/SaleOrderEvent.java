
package com.bytechainx.psi.web.epc.event.sale;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.model.SaleOrder;
import com.bytechainx.psi.common.model.SaleOrderCost;
import com.bytechainx.psi.common.model.SaleOrderFee;
import com.bytechainx.psi.common.model.SaleOrderFund;
import com.bytechainx.psi.common.model.SaleOrderGoods;
import com.bytechainx.psi.sale.service.SaleOrderService;
import com.bytechainx.psi.web.epc.base.BaseTraderEvent;
import com.jfinal.aop.Aop;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.silkie.epc.EpcErrCode;
import com.silkie.epc.impl.Collision;
import com.silkie.epc.impl.EpcEventParam;

/**
 * 销售单
 * 
 * @author defier
 *
 */
public class SaleOrderEvent extends BaseTraderEvent {

	private static final Log LOG = Log.getLog(SaleOrderEvent.class);
	private String action; // 执行的方法名，通过这个参数判断需要执行什么行为
	private List<Object> paramList; // 参数列表，按照接口方法顺序存入

	public SaleOrderEvent(String action) {
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
		SaleOrderService saleOrderService = Aop.get(SaleOrderService.class);
		Ret ret = Ret.ok();
		if(StringUtils.equals(action, "create")) {
			Object saleOrderObject = paramList.get(0);
			if(saleOrderObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			Object orderGoodListObject = paramList.get(1);
			if(orderGoodListObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			Object orderFundListObject = paramList.get(2);
			if(orderFundListObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			Object orderFeeListObject = paramList.get(3);
			if(orderFeeListObject == null) {
			}
			Object orderCostListObject = paramList.get(4);
			if(orderCostListObject == null) {
			}
			ret = saleOrderService.create((SaleOrder) saleOrderObject, (List<SaleOrderGoods>)orderGoodListObject, (List<SaleOrderFund>)orderFundListObject, (List<SaleOrderFee>)orderFeeListObject, (List<SaleOrderCost>)orderCostListObject);
			
		} else if(StringUtils.equals(action, "update")) {
			Object saleOrderObject = paramList.get(0);
			if(saleOrderObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			Object orderGoodListObject = paramList.get(1);
			if(orderGoodListObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			Object orderFundListObject = paramList.get(2);
			if(orderFundListObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			Object orderFeeListObject = paramList.get(3);
			if(orderFeeListObject == null) {
			}
			Object orderCostListObject = paramList.get(4);
			if(orderCostListObject == null) {
			}
			ret = saleOrderService.update((SaleOrder) saleOrderObject, (List<SaleOrderGoods>)orderGoodListObject, (List<SaleOrderFund>)orderFundListObject, (List<SaleOrderFee>)orderFeeListObject, (List<SaleOrderCost>)orderCostListObject);
			
		} else if(StringUtils.equals(action, "disable")) {
			Object idsObject = paramList.get(0);
			if(idsObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			ret = saleOrderService.disable((List<Integer>) idsObject);
			
		} else if(StringUtils.equals(action, "audit")) {
			Object idsObject = paramList.get(0);
			if(idsObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			Object auditStatusObject = paramList.get(1);
			if(auditStatusObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			Object auditDescObject = paramList.get(2);
			if(auditDescObject == null) {
				auditDescObject = "";
			}
			Object auditorIdObject = paramList.get(3);
			if(auditorIdObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			ret = saleOrderService.audit((List<Integer>) idsObject, (AuditStatusEnum)auditStatusObject, (String)auditDescObject, (Integer)auditorIdObject);
		}
		responseMsg(ret);
	}


	@Override
	public Collision getCollision() {
		return Collision.generateCollision("tenant.org.id.0");
	}

}
