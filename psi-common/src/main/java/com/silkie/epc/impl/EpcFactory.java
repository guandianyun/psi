/**
 * Copyright (c) 2014 by pw186.com.
 * All right reserved.
 */
package com.silkie.epc.impl;

import com.silkie.epc.Epc;




/**
 * Epc工厂，对外提供Epc的构造方法
 *
 */
public class EpcFactory {
	
	/**
	 * 非常简单的epc，按时间排序，挨个处理
	 * @return
	 */
	public static Epc createSimpleEpc() {
		return new SimpleEpc();
	}
	
	/**
	 * 采用线程池，并发处理事件的EPC
	 * @return
	 */
	public static Epc createThreadPoolEpc() {
		return createThreadPoolEpc(ThreadPoolEpc.DEFAULT_POOL_SIZE, ThreadPoolEpc.DEFAULT_POOL_SIZE);
	}
	
	/**
	 * 采用线程池，并发处理事件的EPC
	 * @param maxPoolSize 线程池最大线程数
	 * @return
	 */
	public static Epc createThreadPoolEpc(int maxPoolSize) {
		return createThreadPoolEpc(maxPoolSize, maxPoolSize);
	}
	
	/**
	 * 采用线程池，并发处理事件的EPC
	 * @param corePoolSize 线程池最小线程数
	 * @param maxPoolSize 线程池最大线程数
	 * @return
	 */
	public static Epc createThreadPoolEpc(int corePoolSize, int maxPoolSize) {
		Epc epc = new ThreadPoolEpc(corePoolSize, maxPoolSize);
		//TODO set epc to jmx
		//Registry.getJmxManager().registerMBean(new TsMBean<Epc>(epc), "EPC");
		return epc;
	}	
}
