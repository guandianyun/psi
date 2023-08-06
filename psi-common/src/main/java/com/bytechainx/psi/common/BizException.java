/**
 * 
 */
package com.bytechainx.psi.common;

/**
 * 业务异常
 * 
 * @author defier
 *
 */
public class BizException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BizException(String message) {
		super(message);
	}

}
