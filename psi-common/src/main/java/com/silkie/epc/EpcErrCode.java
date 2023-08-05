package com.silkie.epc;

/**
 * 错误码表
 * @author defier
 * @since 1.0
 */
public class EpcErrCode {
	/**
	 * 操作成功，成功的通用返回码
	 */
	public static final int SUCCESS = 0;

	/**
	 * 未知错误
	 */
	public static final int UNKNOWN_ERR = 9999;

	/**
	 * 非法的SessionId
	 */
	public static final int INVALID_SESSION_ID = 1001;

	/**
	 * 非法的消息格式
	 */
	public static final int INVALID_MSG_FORMAT = 1002;

	/**
	 * 系统内部错误
	 */
	public static final int INTERNAL_ERR = 2001;

	/**
	 * 数据库错误
	 */
	public static final int DB_ERR = 2101;

	/**
	 * 通讯错误
	 */
	public static final int SOCKET_ERR = 2201;

	/**
	 * 非法事件参数
	 */
	public static final int INVALID_PARAM = 2301;
	
	/**
	 * 服务器状态异常
	 */
	public static final int SERVER_STATUS_ERROR = 2302;

	
}
