
package com.bytechainx.psi.web.epc.event.inventory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.model.PurchaseRejectOrder;
import com.bytechainx.psi.common.model.PurchaseRejectOrderCost;
import com.bytechainx.psi.common.model.PurchaseRejectOrderFee;
import com.bytechainx.psi.common.model.PurchaseRejectOrderFund;
import com.bytechainx.psi.common.model.PurchaseRejectOrderGoods;
import com.bytechainx.psi.purchase.service.PurchaseRejectOrderService;
import com.bytechainx.psi.web.epc.base.BaseTraderEvent;
import com.jfinal.aop.Aop;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.silkie.epc.EpcErrCode;
import com.silkie.epc.impl.Collision;
import com.silkie.epc.impl.EpcEventParam;

/**
 * 进货退货单
 * 
 * @author defier
 *
 */
public class PurchaseRejectOrderEvent extends BaseTraderEvent {

	private static final Log LOG = Log.getLog(PurchaseRejectOrderEvent.class);
	private String action; // 执行的方法名，通过这个参数判断需要执行什么行为
	private List<Object> paramList; // 参数列表，按照接口方法顺序存入

	public PurchaseRejectOrderEvent(String action) {
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
		PurchaseRejectOrderService purchaseRejectOrderService = Aop.get(PurchaseRejectOrderService.class);
		Ret ret = Ret.ok();
		if(StringUtils.equals(action, "create")) {
			Object purchaseRejectOrderObject = paramList.get(0);
			if(purchaseRejectOrderObject == null) {
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
			ret = purchaseRejectOrderService.create((PurchaseRejectOrder) purchaseRejectOrderObject, (List<PurchaseRejectOrderGoods>)orderGoodListObject, (List<PurchaseRejectOrderFund>)orderFundListObject, (List<PurchaseRejectOrderFee>)orderFeeListObject, (List<PurchaseRejectOrderCost>)orderCostListObject);
			
		} else if(StringUtils.equals(action, "update")) {
			Object purchaseRejectOrderObject = paramList.get(0);
			if(purchaseRejectOrderObject == null) {
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
			ret = purchaseRejectOrderService.update((PurchaseRejectOrder) purchaseRejectOrderObject, (List<PurchaseRejectOrderGoods>)orderGoodListObject, (List<PurchaseRejectOrderFund>)orderFundListObject, (List<PurchaseRejectOrderFee>)orderFeeListObject, (List<PurchaseRejectOrderCost>)orderCostListObject);
			
		} else if(StringUtils.equals(action, "disable")) {
			Object idsObject = paramList.get(0);
			if(idsObject == null) {
				responseMsg(Ret.fail("参数异常"));
				return;
			}
			ret = purchaseRejectOrderService.disable((List<Integer>) idsObject);
			
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
			ret = purchaseRejectOrderService.audit((List<Integer>) idsObject, (AuditStatusEnum)auditStatusObject, (String)auditDescObject, (Integer)auditorIdObject);
		}
		responseMsg(ret);
	}


	@Override
	public Collision getCollision() {
		return Collision.generateCollision("tenant.org.id.0");
	}

}
