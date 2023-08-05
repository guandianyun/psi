/**
 * Copyright (c) 2014 by pw186.com.
 * All right reserved.
 */
package com.silkie.epc.impl;

import java.util.HashMap;
import java.util.Map;

import com.silkie.epc.BaseParam;
import com.silkie.epc.ModeLock;

/**
 * 事件参数，改接口实例将作为参数注入Event中执行
 *
 */
public class EpcEventParam extends BaseParam {
	
	private int seqSeries;// unsigned short
	private long seqNo; // unsigned int
	private String tid;
	
	private ModeLock modeLock;
	
	
	private HashMap<String, Object> map;
	private long timestamp;
	
	public EpcEventParam() {
	    timestamp = System.currentTimeMillis();
		map = new HashMap<String, Object>();
	}
	
	@Override
	protected Map<String, Object> getMap() {
		return map;
	}

	/**
	 * 参数类生成的时间
	 */
	public long getCreatedMillis() {
	    return timestamp;
	}

	public void setSeqSeries(int seqSeries) {
		this.seqSeries = seqSeries;
	}

	public int getSeqSeries() {
		return seqSeries;
	}

	public void setSeqNo(long seqNo) {
		this.seqNo = seqNo;
	}

	public long getSeqNo() {
		return seqNo;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getTid() {
		return tid;
	}
	
	public ModeLock getModeLock() {
		return modeLock;
	}

	public void setModeLock(ModeLock modeLock) {
		this.modeLock = modeLock;
	}
	
}
