/**
 * Copyright (c) 2014 by pw186.com.
 * All right reserved.
 */
package com.silkie.epc;

import com.silkie.epc.impl.Collision;


/**
 * 事件处理中心接口
 *
 */
public interface Epc {

	/**
	 * 添加一个待处理事件到epc中
	 * EPC保证拥有相同冲突体的事件不会被并发被执行
	 * 
	 * @param event 待处理事件
	 * @param collis 该事件对应的冲突体，null表示不会与其它事件有冲突
	 */
	void pushEvent(EpcEvent event, Collision collis);
	
	/**
	 * 处理完所有待处理事件后，关闭epc
	 */
	void shutdown();
	
	/**
	 * 立刻关闭epc，丢弃所有待处理事件
	 */
	void shutdownNow();
}
