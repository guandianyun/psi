/**
 * Copyright (c) Since 2014, Power by Pw186.com
 */
package com.bytechainx.psi.web.epc;


import java.util.ArrayList;
import java.util.List;

import com.bytechainx.psi.common.kit.RandomUtil;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.silkie.epc.EpcEvent;
import com.silkie.epc.ModeLock;
import com.silkie.epc.impl.EpcEventParam;
import com.silkie.epc.impl.ThreadPoolEpc;


/**
 * 消息生产者
 * 
 * @version 1.0
 */
public class TraderEventProducer {

	private static final Log LOG = Log.getLog(TraderEventProducer.class);
	@Inject
	private ThreadPoolEpc epc;
	@Inject
	private ModeLock modeLock;
	
	public Ret request(Integer adminId, EpcEvent event, Object... params) {
		try {
			modeLock.lock();
			
			EpcEventParam param = new EpcEventParam();
			param.setModeLock(modeLock);
			param.setValue("admin_id", adminId);
			
			if(params != null) {
				List<Object> paramsList = new ArrayList<>();
				for (int i = 0; i < params.length; i++)  {
					paramsList.add(params[i]);
				}
				param.setValue(paramsList.getClass().getName(), paramsList);
			}
			String requestId = RandomUtil.genUUID();
			param.setValue("request_id", requestId);
			
			event.setEventParam(param);
			epc.pushEvent(event, event.getCollision());
			
			modeLock.await();
			
			EpcEventParam respParam = event.getResponseParam();
			if(respParam == null) {
				return Ret.ok("正在处理中").set("request_id", requestId); // 如果有request_id返回，前端需要轮询结果
			}
			
			Ret ret = (Ret) respParam.getObjValue(Ret.class.getName());
			return ret;
			
		} catch (Exception e) {
			LOG.error("发送请求异常！！！", e);
			return Ret.fail("处理异常:"+e.getMessage());
		} finally {
			modeLock.unlock();
		}
	}
	
	public EpcEventParam sendRequestMessage(EpcEvent event, Integer adminId, Object... params) throws Exception {
		try {
			modeLock.lock();
			
			EpcEventParam param = new EpcEventParam();
			param.setValue("admin_id", adminId);
			
			if(params != null) {
				for (int i = 0; i < params.length; i++)  {
					Object p = params[i];
					param.setValue(p.getClass().getName(), p);
				}
			}
			
			event.setEventParam(param);
			epc.pushEvent(event, event.getCollision());
			
			modeLock.await();

		} catch (Exception e) {
			LOG.error("发送请求异常！！！", e);
		} finally {
			modeLock.unlock();
		}
		return event.getResponseParam();
	}

}
