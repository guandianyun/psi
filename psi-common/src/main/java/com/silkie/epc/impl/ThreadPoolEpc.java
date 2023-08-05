/**
 * Copyright (c) 2014 by pw186.com.
 * All right reserved.
 */
package com.silkie.epc.impl;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.jfinal.log.Log;
import com.silkie.epc.EpcEvent;
import com.silkie.epc.MillsCounter;



/**
 * 多线程并发处理事件，提高事件的处理效率
 * 并保证拥有相同Collision的事件不会被同时执行
 * 
 */
public class ThreadPoolEpc extends BaseEpc {
	private static final Log LOG = Log.getLog(ThreadPoolEpc.class);
	
	/* 缺省大小为 cpu个数的 2倍 */
	static final int DEFAULT_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
	/* 缺省最大线程数为 cpu个数的4倍  */
	static final int DEFAULT_MAX_SIZE = Runtime.getRuntime().availableProcessors() * 4;
	
	// 线程池维护线程的最小数量
	int corePoolSize = DEFAULT_POOL_SIZE;
	
	// 线程池维护线程的最大数量
	int maxPoolSize = DEFAULT_MAX_SIZE;
	
	// 正在处理的任务数量,逻辑上应保证该数字 不大于  线程的数量(poolSize)
	AtomicInteger runningTaskNum = new AtomicInteger(0);
	
	// 执行任务的线程池，事件是伪装成Task(任务)执行的。
	ThreadPoolExecutor executor = null;
	
	// 主队列，用于存放尚未处理的(事件)任务的队列
	ConcurrentLinkedQueue<Task> mainQueue = null;
	
	// 冲突等待队列表，用于存放等待同类型冲突(Collision)处理完成后，再执行的任务队列；每种冲突(Collision)一个队列
	ConcurrentHashMap<Collision, ConcurrentLinkedQueue<Task>> waitingCollsQueueMap = null;
	
	// 冲突执行表，用于存放正在执行任务的占用冲突(Collision)；占用冲突被释放后，等待的任务才能执行
	ConcurrentHashMap<Collision, Task> runningCollsMap = null;
	
	// 等待执行的顶级冲突体
	Task godTask = null;
	
	// 加入event队列锁，防止多线程加入Event时，出现错误。
	ReentrantLock pushLock = new ReentrantLock();
	
	// 执行任务的耗费毫秒数统计
	MillsCounter runningCounter = new MillsCounter();
	
	// 等待任务的耗费毫秒数统计
	MillsCounter waitingCounter = new MillsCounter();
	
	// 是否关闭中
	boolean isShutdown = false;
	public ThreadPoolEpc() {
		this(DEFAULT_POOL_SIZE, DEFAULT_MAX_SIZE);
	}
	public ThreadPoolEpc(int pool) {
		this(pool, DEFAULT_MAX_SIZE);
	}
	// 实际使用中发现，线程池的个数不会增长到max，很奇怪
	// 建议初始化时，pool=max
	public ThreadPoolEpc(int pool, int max) {
		this.corePoolSize = (pool < 1)? DEFAULT_POOL_SIZE : pool;
		this.maxPoolSize = (max < 1)? DEFAULT_MAX_SIZE : max;
		this.maxPoolSize = (maxPoolSize < corePoolSize)? corePoolSize : maxPoolSize;
		
		executor = createThreadPoolExecutor(corePoolSize, maxPoolSize);
		
		mainQueue = new ConcurrentLinkedQueue<Task>();
		waitingCollsQueueMap = new ConcurrentHashMap<Collision, ConcurrentLinkedQueue<Task>>();
		runningCollsMap = new ConcurrentHashMap<Collision, Task>();
	}
	
	@Override
	public void pushEvent(EpcEvent event, Collision collis) {
		if (isShutdown) return;
		if (event == null)
			throw new NullPointerException("Event is null......");
		
		Task task = new Task(event, collis);
		//开始等待任务计时
		task.beginProfile();
		
		// 先转换为任务，存入主队列
		mainQueue.offer(task);
		
		// 检测主队列看有无任务可做
		checkMainQueue();
		
		// 检测死锁
		checkDeadlock();
	}	

	@Override
	protected void afterRun(Task t) {
		pushLock.lock();
		try {
			endTask(t);
		} finally {
			pushLock.unlock();
		}
		
		if (t.getCollision() != null) {// 检测 冲突队列，看有无任务可做
			if(checkCollisionQueue(t.getCollision())) {
				return; // 如果有任务被激活 就不再往下检测了
			}
		}
		
		// 检测死锁
		checkDeadlock();
		
		checkMainQueue();//再检测主队列
		
		if (isShutdown && !haveWaitingTask())// 没有等待的 就可以关闭epc了
			executor.shutdown();
	}

	@Override
	protected void beforeRun(Task t) {
//System.out.println("1111111111>>>"+runningCollsMap.size()+";;;;>>"+runningCollsMap.keySet());	
	}
	
	// 检测主队列是否有可执行的事件
	private void checkMainQueue(){
		int cur = runningTaskNum.get();// 当前并发执行的任务数
		if (cur >= maxPoolSize)//已到最大任务数，等待有任务执行完后，再执行。
			return;
		
		pushLock.lock();
		try {
			cur = runningTaskNum.get();// 此处的并发数可能已经改变
			if (godTask != null) {// 有顶级任务 正在等候
				if (cur == 0) {// 当前没有任务在执行
					cur++;//并发任务数 加一
					beginTask(godTask);// 执行任务
				} 
			}
			
			while((godTask == null) // 没有顶级冲突体等待执行
					&& (!mainQueue.isEmpty()) // 主队列不为空
					&& (cur < maxPoolSize)) { // 正在运行的任务数 小于 线程池数
				Task task = mainQueue.poll();
				Collision colls = task.getCollision();
				if (colls == null) {// 无冲突要求的任务，直接加入执行池
					cur++;//并发任务数 加一
					beginTask(task);// 执行任务
				} else if (colls.equals(Collision.GOD_COLLISION)) {// 该任务为 顶级冲突任务
					// 需要等待当前在执行或在等待的任务执行 执行完后，再执行顶级冲突任务
					godTask = task;
					if (cur == 0) {// 当前没有任务在执行
						cur++;//并发任务数 加一
						beginTask(task);// 执行任务
					} 
					return;// 结束循环，因为顶级冲突任务 执行期间 其它任务都不容许执行
				} else if (runningCollsMap.containsKey(colls)) {// 正在运行的任务有同样的冲突
					if (waitingCollsQueueMap.containsKey(colls)) {// 有类似冲突在等待的，加入等待队列
						waitingCollsQueueMap.get(colls).offer(task);
					} else {//之前没有的话， 新建一个队列
						ConcurrentLinkedQueue<Task> queue = new ConcurrentLinkedQueue<Task>();
						queue.offer(task);
						waitingCollsQueueMap.put(colls, queue);
					}
				} else {// 有冲突要求，但没有类似冲突任务 在执行的，加入执行池，并记入执行冲突表中
					cur++;
					beginTask(task);
				}
			}// end while
		} finally {
			pushLock.unlock();
		}
	}
	
	// 检测冲突队列是否有可执行的事件
	// 冲突队列中还有事件可被执行 返回True； 冲突队列已空 返回false
	private boolean checkCollisionQueue(Collision colls) {
		if (colls == null) return false;// null 表示 这个任务没有 冲突要求
		
		pushLock.lock();
		try {
			if (waitingCollsQueueMap.containsKey(colls)) {// 还有类似冲突的任务在等待
				ConcurrentLinkedQueue<Task> queue = waitingCollsQueueMap.get(colls);
				beginTask(queue.poll());
				
				if (queue.isEmpty())
					waitingCollsQueueMap.remove(colls);
				return true;
			}
		} finally {
			pushLock.unlock();
		}
		
		return false;
	}
	
	// 死锁检测，检查线程池是否存在死锁情况
	private void checkDeadlock() {
		pushLock.lock();
		try {
			// 检查 冲突运行队列 和 当前运行线程数 是否匹配
			if (runningTaskNum.get() < runningCollsMap.size()) {// 运行的线程数 比 记录的运行冲突数少，肯定是发生了死锁
				StringBuilder sb = new StringBuilder();// 生成日志
				sb.append("Found Deadlock!!! ");
				sb.append("runningTaskNum is [");
				sb.append(runningTaskNum.get());
				sb.append("], runningCollsMap.size is [");
				sb.append(runningCollsMap.size());
				sb.append("], runningCollsMap.keys is [");
				sb.append(runningCollsMap.keySet());
				sb.append("]; waitingCollsQueueMap.size is [");
				sb.append(waitingCollsQueueMap.size());
				sb.append("]; waitingCollsQueueMap.keys is [");
				sb.append(waitingCollsQueueMap.keySet());
				sb.append("].");
				LOG.error(sb.toString());//写入日志
				
				
				runningCollsMap.clear();// 清除运行任务 冲突，理论上此时本应该为空的，再清一下防止死锁
				
				// 已经死锁的等待任务,每种冲突执行一个
				Set<Collision> colliSet = waitingCollsQueueMap.keySet();
				for (Iterator<Collision> itr = colliSet.iterator(); itr.hasNext();) {
					Collision colls = itr.next();
					
					ConcurrentLinkedQueue<Task> queue = waitingCollsQueueMap.get(colls);
					if (!queue.isEmpty()) {
						beginTask(queue.poll());// 执行
					}
					
					if (queue.isEmpty())
						waitingCollsQueueMap.remove(colls);
				}
			}
		} finally {
			pushLock.unlock();
		}
	}
	
	// 开始执行任务前的准备工作
	private void beginTask(Task task) {
		// 结束等待任务计时
		task.endProfile();
		// 统计任务等待时间和数量
		waitingCounter.count(task.elpasedMills());
		
		// EPC已被强制关闭
		if (executor.isTerminating())
			return;
		
		// 正在处理的任务数 加一
		runningTaskNum.incrementAndGet();
		
		if (task.getCollision() != null 
				&& !task.getCollision().equals(Collision.GOD_COLLISION)) {
			runningCollsMap.put(task.getCollision(), task);
		}
		
		//开始执行任务计时
		task.beginProfile();
		// 执行任务
		executor.execute(task);
	}
	
	// 任务完成后的收尾工作
	private void endTask(Task task) {
		// 结束执行任务计时
		task.endProfile();
		// 统计任务执行时间和数量
		runningCounter.count(task.elpasedMills());
		
		// 正在处理的任务数 减一
		runningTaskNum.decrementAndGet();
		
		if (task.getCollision() != null) {
			if (task.getCollision().equals(Collision.GOD_COLLISION))
				godTask = null;
			else
				runningCollsMap.remove(task.getCollision());// 在执行池中，清除相应的 冲突
		}
	}
	
	// 是否还有任务在map中等待,true还有，false没有
	private boolean haveWaitingTask() {
		return !(mainQueue.size() == 0 && waitingCollsQueueMap.size() == 0);
	}
	
	// 生成固定大小的线程池，执行任务
	private ThreadPoolExecutor createThreadPoolExecutor(int nThreads, int maxThreads) {
        return new ThreadPoolExecutor(nThreads, maxThreads,
                1000L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new DefaultEpcThreadFactory());
        /*{// 压力大的时候有莫名其妙的bug
        	@Override
			protected void afterExecute(Runnable r, Throwable t) {
				super.afterExecute(r, t);
				afterRun((Task)r);
			}
        	
        	@Override
			protected void beforeExecute(Thread t, Runnable r) {
				super.beforeExecute(t, r);
				beforeRun((Task)r);
			}
        };*/
	}
	
    /**
     * The default EPC thread factory
     */
    static class DefaultEpcThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        DefaultEpcThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null)? s.getThreadGroup() :
                                 Thread.currentThread().getThreadGroup();
            namePrefix = "EPC-" +
                          poolNumber.getAndIncrement() +
                         "-worker-";
        }

        @Override
		public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
/////////////////////////////////////////////////////////// for jmx ////////////
	public int getCorePoolSize() {
		return corePoolSize;
	}
	
	public int getMaxPoolSize() {
		return maxPoolSize;
	}
	
	public int getWaitingCollsQueueMapLen() {
		return waitingCollsQueueMap.size();
	}
	
	public String getWaitingCollsQueueMapDetail() {
		Enumeration<Collision> enu = waitingCollsQueueMap.keys();
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		while(enu.hasMoreElements()) {
			Collision col = enu.nextElement();
			sb.append(col);
			sb.append("=");
			ConcurrentLinkedQueue<Task> queue =  waitingCollsQueueMap.get(col);
			sb.append(queue == null? "null" : queue.size());
			sb.append(";");
		}
		sb.append(")");
		
		return sb.toString();
	}
	
	public int getRunningCollsMapLen() {
		return runningCollsMap.size();
	}
	
	public int getMainQueueLen() {
		return mainQueue.size();
	}
	
	public int getRunningTaskNum() {
		return runningTaskNum.get();
	}
	
	public long getRunningTaskCount() {
		return runningCounter.getRunCount();
	}
	
	public long getRunningMillsCount() {
		return runningCounter.getMillsCount();
	}
	
	public String printRunningCounter() {
		return runningCounter.toString();
	}
	
	public long getWaitingTaskCount() {
		return waitingCounter.getRunCount();
	}
	
	public long getWaitingMillsCount() {
		return waitingCounter.getMillsCount();
	}	
	
	public String printWaitingCounter() {
		return waitingCounter.toString();
	}
	
	public long getTaskCount() {
		return executor.getTaskCount();
	}
	
	public long getCompletedTaskCount() {
		return executor.getCompletedTaskCount();
	}
	
	public int getAticveCount() {
		return executor.getActiveCount();
	}
	
	public int getThreadPoolQueueSize() {
		return executor.getQueue().size();
	}
	
	String dump() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(";getWaitingCollsQueueMapLen=");
		sb.append(getWaitingCollsQueueMapLen());
		sb.append(";getRunningCollsMapLen=");
		sb.append(getRunningCollsMapLen());
		sb.append(";getMainQueueLen=");
		sb.append(getMainQueueLen());
		sb.append(";getRunningCounter=");
		sb.append(runningCounter);
		sb.append(";getWaitingCounter=");
		sb.append(waitingCounter);
		sb.append(";getTaskCount=");
		sb.append(getTaskCount());
		sb.append(";getCompletedTaskCount=");
		sb.append(getCompletedTaskCount());	
		sb.append(";getAticveCount=");
		sb.append(getAticveCount());
		sb.append(";getThreadPoolQueueSize=");
		sb.append(getThreadPoolQueueSize());	
		sb.append(";getWaitingCollsQueueMapDetail=");
		sb.append(getWaitingCollsQueueMapDetail());	
		
		return sb.toString();
	}
	@Override
	public void shutdown() {
		isShutdown = true;
		if (isShutdown && !haveWaitingTask())// 没有等待的 就可以关闭epc了
			executor.shutdown();
		
	}
	@Override
	public void shutdownNow() {
		isShutdown = true;
		executor.shutdownNow();
		
		mainQueue.clear();
		waitingCollsQueueMap.clear();
		runningCollsMap.clear();
	}
}
