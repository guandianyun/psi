/**
 * Copyright (c) 2014 by pw186.com.
 * All right reserved.
 */
package com.silkie.epc;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 统计运行时间的毫秒数，统计对象包括：
 * 	执行次数，总耗时，耗时最短5个时间，耗时最长5个时间
 *
 */
public class MillsCounter {
	public static final String LINE_SEP = System.getProperty("line.separator", "\r\n");
	
	static final int DEFAULT_SIZE = 5; 
	
	// 总耗时
	long millisCount;
	// 总执行次数
	long runCount;
	// 耗时最短 X个
	double[] shortest;
	// 耗时最长X个
	double[] longest;
	
	int size;
	
	ReentrantLock syn;
	
	public MillsCounter() {
		this(DEFAULT_SIZE);
	}
	
	public MillsCounter(int size) {
		if (size < 1)
			throw new IllegalArgumentException("Invalid size: " + size);
		
		this.size = size;
		millisCount = 0;
		runCount = 0;
		shortest = new double[size];
		longest = new double[size];
		for (int i = 0; i < size; i++) {
			shortest[i] = Integer.MAX_VALUE;
			longest[i] = 0;
		}
		
		syn = new ReentrantLock();
	}
	
	/**
	 * 统计一次运行的 毫秒数
	 * @param mills 本次运行的毫秒
	 */
	public void count(long mills) {
		count((double)mills);
	}
	
	/**
	 * 统计一次运行的 毫秒数，支持小数
	 * @param mills 本次运行的毫秒，小数
	 */
	public void count(double mills) {
		syn.lock();
		try {
			millisCount += mills;
			runCount++;
			
			doShort(mills);
			doLong(mills);
		} finally {
			syn.unlock();
		}
	}
	
	private void doLong(double mills) {
		if (longest[0] < mills) {
			longest[0] = mills;
			Arrays.sort(longest);
		}
	}

	private void doShort(double mills) {
		if (shortest[size - 1] > mills) {
			shortest[size - 1] = mills;
			Arrays.sort(shortest);
		}
	}
	
	public long getRunCount() {
		return runCount;
	}
	
	public long getMillsCount() {
		return millisCount;
	}
	
	public long getAvgMills() {
		return runCount == 0? 0 : millisCount/runCount;
	}
	
	public double getShortestMills() {
		return shortest[0];
	}
	
	public double getLongestMills() {
		return longest[size - 1];
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Avg/Total/Num: ");
		sb.append(getAvgMills());
		sb.append("/");
		sb.append(getMillsCount());
		sb.append("/");
		sb.append(getRunCount());
		sb.append(";");
		//sb.append(LINE_SEP);
		
		sb.append("Longest(");
		sb.append(size);
		sb.append("): ");
		for (int i = size - 1; i > 0; i--) {
			sb.append(longest[i]);
			sb.append("/");
		}
		sb.append(longest[0]);
		sb.append(";");
		//sb.append(LINE_SEP);
		
		sb.append("Shortest(");
		sb.append(size);
		sb.append("): ");
		for (int i = 0; i < size - 1; i++) {
			sb.append(shortest[i]);
			sb.append("/");
		}
		sb.append(shortest[size - 1]);
		sb.append(";");
		//sb.append(LINE_SEP);
		
		return sb.toString();
	}
}
