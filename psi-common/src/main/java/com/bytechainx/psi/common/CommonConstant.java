package com.bytechainx.psi.common;

import java.math.BigDecimal;

/**
 * 常量表
 * @author defier.lai
 *
 * 2012-11-18 下午4:50:57
 */
public final class CommonConstant {
	
	public static final String SESSION_ID_KEY = "session_id_key";
	
	public static final String SESSION_AUTH_CODE_RANDOM_KEY = "session_auth_code_random_key";

	public static final String MOBILE_AUTH_CODE_KEY = "mobile.auth.code.key";
	
	public static final String CACHE_NAME_MEM_STORE = "mem.store";
	
	public static final String CACHE_NAME_SESSION_MEM_STORE = "session.mem.store";
	
	public static final String CACHE_NAME_DISK_STORE = "disk.store"; // 缓存到磁盘
	
	public static final String CACHE_NAME_ONE_MINUTE_STORE= "one.minute.store";
	
	public static final String CACHE_NAME_FIVE_MINUTE_STORE= "five.minute.store";
	
	public static final String CACHE_NAME_THIRTY_MINUTE_STORE= "thirty.minute.store";
	
	public static final String CACHE_NAME_ONE_HOUR_STORE= "one.hour.store";
	
	public static final String CACHE_NAME_ONE_DAY_STORE= "one.day.store";
	
	public final static String SESSION_SMS_CODE = "session_sms_code"; // 短信验证码
	
	public final static String COOKIE_TENANT_URL_CODE = "cookie_tenant_url_code"; // cookie缓存租户URLCODE

	public static final String SESSION_TENANT_ID = "session_tenant_id"; // 租户ID
	
	public static final String SESSION_STORE_ID = "session_store_id"; // 门店ID

	public static final BigDecimal PRODUCT_ACCOUNT_PRICE = new BigDecimal(199); // 系统帐户单价/每年
	
	public static final BigDecimal SMS_PRICE = new BigDecimal(0.1); // 短信价格
	
	public final static String SIGNATURE = "signature"; // 交易中心API签名, 随机码+请求时间+私钥，MD5
	public final static String RANDOM_CODE = "randomCode"; // 交易中心API随机码
	public final static String CURRENT_TIME = "currentTime"; // 交易中心API请求的时间
	
	public static final String ONLINE_USER_ID_APP_CACHE = "online.user.id.app.cache."; // app在线用户
	public static final String ONLINE_USER_ID_PC_CACHE = "online.user.id.pc.cache."; // PC在线用户
	
}
