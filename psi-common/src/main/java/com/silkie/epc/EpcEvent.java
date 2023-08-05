/**
 * Copyright (c) 2014 by pw186.com.
 * All right reserved.
 */
package com.silkie.epc;

import com.silkie.epc.impl.Collision;
import com.silkie.epc.impl.EpcEventParam;




/**
 * 处理事件，每个事件都有相关的传入参数和冲突体
 * 		传入参数(EventParam)：事件执行时，必须用到的相关参数。
 *		冲突体(Collision)：事件并发执行时，需要锁定的资源，拥有相同冲突体的事件，不应该被并发执行，必须按顺序执行。
 *
 */
public interface EpcEvent {
	
	/**
	 * 获得该事件执行时需要的传入参数
	 * @return 事件需要的传入参数
	 */
	EpcEventParam getEventParam();
	
	/**
	 * 获得该事件执行时需要的传入参数
	 * @param 事件需要的传入参数
	 */
	void setEventParam(EpcEventParam param);
	
	/**
	 * 获得该事件执行时需要响应的结果参数
	 * @return 事件需要的传入参数
	 */
	EpcEventParam getResponseParam();
	
	/**
	 * 执行事件该事件
	 */
	void execute();
	
	/**
	 * 事件对应的冲突体
	 * @return
	 */
	Collision getCollision();
}
