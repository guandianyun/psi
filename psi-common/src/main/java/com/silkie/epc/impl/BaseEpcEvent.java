/**
 * Copyright (c) 2014 by pw186.com.
 * All right reserved.
 */
package com.silkie.epc.impl;

import java.math.MathContext;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.silkie.epc.EpcErrCode;
import com.silkie.epc.EpcEvent;
import com.silkie.epc.MillsCounter;
import com.silkie.epc.PerfTrace;
import com.silkie.util.DoubleUtil;

/**
 * 所有事件的记录，用来处理某些通用操作
 * @author defier
 * 2014年12月12日 下午3:48:27
 * @version 1.0
 */
public abstract class BaseEpcEvent implements EpcEvent {
	private final static Logger LOG = LoggerFactory.getLogger(BaseEpcEvent.class);
	
	protected EpcEventParam param = null;
	
	protected EpcEventParam responseParam = null;
	
	// 性能调试
	protected PerfTrace perfTrace;
	
	// 事件名称
	private String eventName;

	/**
	 * 事件名称，需要保证全局唯一
	 * 
	 * @return 唯一标识的事件名称
	 */
	public String getName() {
		if (eventName == null)
			eventName = this.getClass().getSimpleName();
		return eventName;
	}
	
	@Override
	public EpcEventParam getEventParam() {
		return param;
	}

	@Override
	public void setEventParam(EpcEventParam param) {
		this.param = param;
	}

	@Override
	public Collision getCollision() {
		return null;
	}
	
	/**
	 * 执行事件，调用回调方法处理整个执行过程
	 * 
	 * @param event
	 *            被执行的事件
	 */
	@Override
	public void execute() {
		// 开启 性能调试
		perfTrace = new PerfTrace(getClass().getSimpleName(), false);// release必须设置为false
		// ；
		try {
			int errCode;
			perfTrace.begin();
			beforeExecute();
			perfTrace.step("before-事件运行");
			try {
				errCode = checkParam(param);
				perfTrace.step("checkParam()");
			} catch (Exception ex) {
				LOG.error("Unhandled check param exception:", ex);
				errCode = EpcErrCode.INVALID_PARAM;// 一个特定的错误号
			}

			if (errCode == EpcErrCode.SUCCESS) {
				doBiz();
				perfTrace.step("doBiz()");
			} else {
				doParamError(errCode);
				perfTrace.step("doParamError()");
			}
		} catch (Exception ex) {
			handleExecption(ex);
			perfTrace.step("handleExecption()");
		} finally {
			afterExecute();
			perfTrace.step("afterExecute()");
			// 清理参数
			param.clear();
			param = null;

			perfTrace.end();
		}
	}

	/**
	 * 统一处理 参数 不合法的情况，由使用epc的各个应用去实现。
	 * 
	 * @param errCode
	 *            错误号
	 * @throws Exception
	 */
	public abstract void doParamError(int errCode) throws Exception;

	/**
	 * 对事件参数进行的合法性检查， 确认参数是否正确，是否完整，是否拥有足够的权限执行此参数 等。
	 * 
	 * @param param
	 *            事件参数
	 * @return 0：表示参数合法；其它不合法行为对应的errCode
	 * @throws Exception
	 *             检查过程产生的异常
	 */
	protected abstract int checkParam(EpcEventParam param) throws Exception;

	/**
	 * 处理事件的业务逻辑
	 */
	protected abstract void doBiz() throws Exception;

	/**
	 * 事件执行之前被调用 可在此方法中准备上下文环境，进行统计等
	 * 
	 * @throws Exception
	 */
	protected void beforeExecute() {
		// 记录下各个opcode的事件执行时间
		beginMeasure();
//		log.debug("Prepare to execute event.");

	}

	/**
	 * 事件执行之后被调用 可在此方法中销毁上下文环境，进行统计等
	 */
	protected void afterExecute() {

		// 记录下各个opcode的事件执行时间
		endMeasure();
//		log.debug("Executed event over.");
	}


	/**
	 * 处理事件执行过程中产生的异常
	 * 
	 * @param ex
	 *            执行事件时抛出的异常
	 */
	protected void handleExecption(Exception ex) {
		if (LOG.isErrorEnabled())
			LOG.error("event error:", ex);
		try {
			handleExceptionResponse(param);
		} catch (Exception e) {
			LOG.error("send unknown err exception message error", e);
		}
	}
	
	protected abstract void handleExceptionResponse(EpcEventParam param);
	

	// 生成回包的EventParam，并设置一些缺省参数
	protected EpcEventParam createEventParam() {
		EpcEventParam ep = new EpcEventParam();
		//FIXME 可以加上参数
		return ep;
	}

	// 记录各个事件运行时间
	static final ConcurrentHashMap<String, MillsCounter> measure = new ConcurrentHashMap<String, MillsCounter>();

	private long beginMeasure, endMeasure;

	private void beginMeasure() {
		beginMeasure = System.nanoTime();
	}

	private void endMeasure() {
		endMeasure = System.nanoTime();
		double elapsed = DoubleUtil.divide(new MathContext(3),
				(endMeasure - beginMeasure), 1000000L);
		doMeasure(getName(), elapsed);
		if (LOG.isDebugEnabled())
			LOG.debug(getName() + " elapsed: " + elapsed + " ms.");
	}

	void doMeasure(String eventname, double elapsed) {
		if (!measure.containsKey(eventname)) {
			measure.put(eventname, new MillsCounter());
		}
		measure.get(eventname).count(elapsed);
	}
	
	public void setResponseParam(EpcEventParam responseParam) {
		this.responseParam = responseParam;
	}
	@Override
	public EpcEventParam getResponseParam() {
		return responseParam;
	}

}
