/**
 * 
 */
package com.bytechainx.psi.common.dto;

/**
 * 条件查询对象
 * @author defier
 *
 */
public class ConditionFilter {

	private Operator operator; // 运算符
	private Object value; // 条件值
	
	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public enum Operator {
		eq, // =，相等
		more, // 多个条件
		neq, // != 不相等
		like, // like %% 模糊查询
		in, // in (xxx)
		notIn, // not in (xxx)
		gt, // 大于
		lt // 小于
	}
}

