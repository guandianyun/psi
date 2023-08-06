/**
 * 
 */
package com.bytechainx.psi.common.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.CommonConfig;
import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.kit.RandomUtil;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;

import cn.hutool.http.HttpUtil;

/**
 * 交易中心接口
 * @author defier
 *
 */
public class TraderCenterApi {
	
	private static final Log LOG = Log.getLog(TraderCenterApi.class);
	/**
	 * 请求交易中心接口
	 * @param adminId 当前登录用户
	 * @param tenantOrgId 租户ID
	 * @param params
	 * @return
	 */
	public static String requestApi(String api, Integer adminId, Map<String, String[]> params) {
		try {
			String randomCode = RandomUtil.genRandomNum(5);
			long currentTime = System.currentTimeMillis();
			String signature = DigestUtils.md5Hex(randomCode+currentTime+CommonConfig.traderApiSecret); // 签名
			
			Map<String, Object> _params = new HashMap<>();
			_params.put(CommonConstant.RANDOM_CODE, randomCode);
			_params.put(CommonConstant.CURRENT_TIME, currentTime);
			_params.put(CommonConstant.SIGNATURE, signature);
			_params.put("admin_id", adminId+"");
			
			params.forEach((key, values) -> {
				_params.put(key, values);
			});
			
			String response = HttpUtil.post(CommonConfig.traderApiDomain+api, _params);
			JSONObject result = JSONObject.parseObject(response);
			
			return result.toJSONString();
			
		} catch (Exception e) {
			LOG.error("调用交易中心异常, api："+ api, e);
			return Ret.fail("调用接口异常["+api+"],"+e.getMessage()).toJson();
		}
	}
	
	public static String requestApix(String api, Integer adminId, Map<String, Object> params) {
		try {
			String randomCode = RandomUtil.genRandomNum(5);
			long currentTime = System.currentTimeMillis();
			String signature = DigestUtils.md5Hex(randomCode+currentTime+CommonConfig.traderApiSecret); // 签名
			
			params.put(CommonConstant.RANDOM_CODE, randomCode);
			params.put(CommonConstant.CURRENT_TIME, currentTime);
			params.put(CommonConstant.SIGNATURE, signature);
			params.put("admin_id", adminId+"");
			
			String response = HttpUtil.post(CommonConfig.traderApiDomain+api, params);
			JSONObject result = JSONObject.parseObject(response);
			
			return result.toJSONString();
			
		} catch (Exception e) {
			LOG.error("调用交易中心异常, api："+ api, e);
			return Ret.fail("调用接口异常["+api+"],"+e.getMessage()).toJson();
		}
	}

}
