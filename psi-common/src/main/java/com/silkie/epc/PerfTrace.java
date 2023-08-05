package com.silkie.epc;

import java.io.PrintStream;

/**
 * 跟踪一个运行过程的每步运行时间，并输出。
 * 该类线程不安全
 *
 */
public class PerfTrace {
    
    long init;
    long last;
    long maxCost;
    
    String name;
    
    PrintStream out;
    
    boolean enable;
    
    public PerfTrace(String name, boolean enable) {
	this(name, System.out, enable);
    }
    
    public PerfTrace(String name, PrintStream out, boolean enable) {
	this.name = name;
	this.out = out;
	this.enable = enable;
    }
    
    public void begin() {
	if (!enable) return;
	init = System.currentTimeMillis();
	last = init;
	print(name+", begin trace at " + last);
    }
    
    public void step() {
	if (!enable) return;
	step(null);
    }
    public void step(String desc) {
	if (!enable) return;
	long cur = System.currentTimeMillis();
	long cost=cur - last;
	print(name+", step: " + desc + ", elapsed: " + (cost) +" ms");
	last = cur;
	if(cost>maxCost){
	    maxCost=cost;
	}
    }
    
    public void end() {
	if (!enable) return;
	long cur = System.currentTimeMillis();
	print(name+", end trace, all elapsed: " + (cur - init) +" ms");
	last = cur;
    }
    
    public long elpased () {
	return last - init;
    }
    
    private void print(String msg) {
	out.println(msg);
    }
}
