/**
 * Copyright (c) 2014 by pw186.com.
 * All right reserved.
 */
package com.silkie.epc.impl;

/**
 * 冲突体，事件并发执行会导致互相冲突的对象
 * 在EPC多线程执行事件时，会保证拥有相同(采用equals()比较)冲突体的事件不会被并发执行，
 * 即：同一时间只会有一个拥有相同冲突体的事件在执行
 *
 */
public class Collision {
	
	public static Collision GOD_COLLISION = new Collision("*");
	
	private Object colls;
	Collision(Object obj) {
		colls = obj;
	}
	
	/**
	 * 根据CustId生成冲突体，保证同一个CustId的冲突体事件不会被同时执行
	 * 
	 * @param custId 客户号
	 * @return 一个冲突体实例
	 */
	public static Collision generateCollision(String custId) {
		return (custId == null)? null : new Collision(custId);
	}
	
	/**
	 * 顶级冲突体，拥有此冲突体的事件，总个epc同一时间只有它在运行，所有线程空闲
	 * @return 顶级冲突体
	 */
	public static Collision gainGodCollision(){
		return GOD_COLLISION;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Collision) {
			if (this == other)
				return true;
			else if (colls == null || ((Collision)other).colls == null)
				return false;
			else 
				return colls.equals(((Collision)other).colls);
		}
		
		return false;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Collision(colls);
	}

	// 放在map里面做key时，此函数返回值相等，containsKey() 才会相等
	@Override
	public int hashCode() {
		return (colls == null)? super.hashCode() : colls.hashCode();
	}
	
	@Override
	public String toString() {
		return "Collision[" + colls.toString() + "]";
	}
	
}
