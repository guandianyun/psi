/**
 * Copyright (c) 2014 by pw186.com.
 * All right reserved.
 */
package com.silkie.epc.impl;

import com.silkie.epc.Epc;
import com.silkie.epc.EpcEvent;

/**
 * epc的一些基础实现
 * @author defier
 * 2014年12月12日 下午3:44:45
 * @version 1.0
 */
public abstract class BaseEpc implements Epc {

	// 任务执行之前被调用
	protected void beforeRun(Task t) { }
	
	// 任务执行之后被调用
	protected void afterRun(Task t) { }
	
	/*
	 * 在线程池中，执行Event的辅助类
	 */
	protected class Task implements Runnable {
		EpcEvent te;
		Collision colli;
		
		Task(EpcEvent event, Collision collis) {
			if (event == null)
				throw new NullPointerException("event is null.");
			
			te = event;
			colli = collis;
		}
		
		public EpcEvent getEvent() {
			return te;
		}
		
		public Collision getCollision() {
			return colli;
		}
		
		/* 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				beforeRun(this);
				
				te.execute();
			} catch(Exception ex) {
				//ex.printStackTrace();
			} finally {
				afterRun(this);
			}
		}

	/////////////////////////////////////////// 记录任务的运行时间
		long tm, em;
		void beginProfile() {
			tm = System.currentTimeMillis();
		}
		
		void endProfile() {
			em = System.currentTimeMillis();
		}
		
		long elpasedMills() {
			return em - tm;
		}
	}
}
