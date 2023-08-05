/**
 * Copyright (c) Since 2014, Power by Pw186.com
 */
package com.silkie.epc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对话模式全局锁，对话模式请求端必须等待响应消息，才进入下一步处理。
 * 如果并发量多，对话模式会遇到瓶颈
 * @author defier
 * 2014年12月27日 下午4:15:46
 * @version 1.0
 */
public class ModeLock {
	
	private final static Logger LOG = LoggerFactory.getLogger(ModeLock.class); 
	
	private static int TIME_OUT_TIME = 3;
	private ReentrantLock dialogLock = new ReentrantLock();
	private final Condition sendCondition = dialogLock.newCondition();
	
	public void await() throws Exception {
		sendCondition.await(TIME_OUT_TIME, TimeUnit.SECONDS);
	}

	public void singal() {
		dialogLock.lock();
		try {
			sendCondition.signal();
		} catch (Exception e) {
			LOG.error("error", e);
		} finally {
			dialogLock.unlock();
		}
	}
	
	public void lock() {
		dialogLock.lock();
	}
	
	public void unlock() {
		dialogLock.unlock();
	}
	
}
