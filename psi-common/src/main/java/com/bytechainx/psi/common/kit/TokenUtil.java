package com.bytechainx.psi.common.kit;

import java.util.UUID;

public class TokenUtil {

	private static final String DEFAULT_TOKEN_PREFIX = "quant:";
	
	/**
	 * APP 创建token
	 * @param userInfoId
	 * @return
	 */
	public static String getToken(Integer userInfoId) {
		return DEFAULT_TOKEN_PREFIX + userInfoId + ":token:" + UUID.randomUUID().toString();
	}
	
	/**
	 * token 搜索
	 * @param userInfoId
	 * @return
	 */
	public static String getLikeToken(Integer userInfoId) {
		return DEFAULT_TOKEN_PREFIX + userInfoId + ":token:*";
	}
	
}
