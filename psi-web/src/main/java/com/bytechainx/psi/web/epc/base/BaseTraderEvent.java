/**
 * Sangame.com Inc. Copyright (c) 2006-2012 All Rights Reserved.
 */
package com.bytechainx.psi.web.epc.base;

import com.bytechainx.psi.common.BizException;
import com.bytechainx.psi.common.TransactionService;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.silkie.epc.impl.BaseEpcEvent;
import com.silkie.epc.impl.EpcEventParam;


/**
 * 交易事件基类
 * 
 * @author defier 
 * 2016年7月16日 下午4:48:35
 * @version 1.0
 */
public abstract class BaseTraderEvent extends BaseEpcEvent {

	private static final Log LOG = Log.getLog(BaseTraderEvent.class);

	protected EpcEventParam eventParam;
	
	private int adminId;
	private String requestId;

	@Override
	protected int checkParam(EpcEventParam param) throws Exception {
		this.eventParam = param;
		this.adminId = param.getIntValue("admin_id");
		this.requestId = param.getStrValue("request_id");
		return checkBizParam(eventParam);
	}

	protected abstract int checkBizParam(EpcEventParam param) throws Exception;

	protected void doBiz() {
		// 业务逻辑处理
		TransactionService transactionService = new TransactionService();
		transactionService.startTransaction();
		try {
			dealBiz();
			transactionService.commitTransaction();
		} catch (Exception e) {
			LOG.error("事件处理异常，交易失败", e);
			transactionService.rollbackTransaction();
			if(e instanceof BizException) {
				responseMsg(Ret.fail(e.getMessage()));
			} else {
				responseMsg(Ret.fail("业务处理异常:"+e.getMessage()));
			}
		} finally {
			transactionService.closeTransaction();
		}

	}

	protected abstract void dealBiz() throws Exception;

	/**
	 * 参数错误的处理方式
	 */
	@Override
	public void doParamError(int errCode) throws Exception {
		responseMsg(Ret.fail("参数错误"));
	}

	@Override
	protected void handleExceptionResponse(EpcEventParam param) {
		responseMsg(Ret.fail("执行异常"));
	}

	public int getAdminId() {
		return adminId;
	}

	public void setAdminId(int adminId) {
		this.adminId = adminId;
	}
	
	/**
	 * 执行完成后返回消息，先推送结果到redis上，然后同时设置返回值，谁快谁就被处理
	 * @param ret
	 */
	protected void responseMsg(Ret ret) {
		commonResponse(ret);
		
		Cache redis = Redis.use();
		redis.lpush(getRequestId(), ret.toJson());
		redis.expire(getRequestId(), 30); // 有效期30S
	}
	
	protected void commonResponse(Ret ret) {
		EpcEventParam responseParam = new EpcEventParam();
		responseParam.setValue(Ret.class.getName(), ret);
		
		setResponseParam(responseParam);
		
		eventParam.getModeLock().singal(); // 解锁，立即返回结果
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}


}
