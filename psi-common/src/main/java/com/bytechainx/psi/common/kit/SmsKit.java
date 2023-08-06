/**
 * 
 */
package com.bytechainx.psi.common.kit;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentHelper;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.CommonConfig;
import com.bytechainx.psi.common.EnumConstant.FlagEnum;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TenantOrg;
import com.bytechainx.psi.common.model.TenantSmsLog;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;

import cn.hutool.crypto.digest.MD5;

/**
 * 短信发送
 * @author defier
 *
 */
public class SmsKit {
	
	private static final Log LOG = Log.getLog(SmsKit.class);
	
	private static final String MYSUBMAIL_URL_API = "https://api-v4.mysubmail.com/sms/send"; // 赛邮短信接口
	private static final String ZHANGJUN_URL_API = "http://sms.izjun.cn/v2sms.aspx"; // 掌骏短信接口
	
	/**
	 * 发送“找回密码”验证码
	 * @return
	 */
	public static Ret forgetPwd(String mobile) {
		String smsCode = RandomUtil.getRandomNum(4);
		String content = "您正在找回密码，验证码：[smsCode]，5分钟内有效";
		content = StringUtils.replace(content, "[smsCode]", smsCode);
		Ret ret = sendCodeSms(mobile, content);
		if(ret.isOk()) {
			ret.set("smsCode", smsCode);
		}
		return ret;
	}
	
	/**
	 * 发送激活短信验证码
	 * @return
	 */
	public static Ret activeCode(String mobile) {
		String smsCode = RandomUtil.getRandomNum(4);
		String content = "您正在激活帐户，验证码：[smsCode]，5分钟内有效";
		content = StringUtils.replace(content, "[smsCode]", smsCode);
		Ret ret = sendCodeSms(mobile, content);
		if(ret.isOk()) {
			ret.set("smsCode", smsCode);
		}
		return ret;
	}
	
	/**
	 * 微信端用户绑定手机号
	 * @param tenantOrgId 
	 * @param mobile
	 * @param code
	 * @return
	 */
	public static Ret sendWeixinBindCode(String mobile, String smsCode) {
		String content = "您正在绑定手机号，验证码：[smsCode]，5分钟内有效";
		content = StringUtils.replace(content, "[smsCode]", smsCode);
		Ret ret = sendCodeSms(mobile, content);
		if(ret.isOk()) {
			ret.set("smsCode", smsCode);
		}
		return ret;
	}
	
	/**
	 * 发送员工激活短信
	 * @param tenantOrgId
	 * @param randomPwd 
	 */
	public static Ret sendAdminActive(TenantAdmin admin, String randomPwd) {
		TenantOrg tenantOrg = TenantOrg.dao.findCacheById();
		String content = "亲爱的[realName], 您的进销存系统帐号已经开通，默认密码是[password]，首次登录要重置密码才能激活帐号，登录链接：[siteDomain]";
		content = StringUtils.replace(content, "[realName]", admin.getRealName()).replace("[password]", randomPwd).replace("[siteDomain]", tenantOrg.getDomain());
		return sendNoticeSms(admin.getMobile(), content);
	}

	/**
	 * 发送验证码短信
	 * 减去租户短信的数量，记录短信日志。
	 * @param tenantOrgId
	 * @param mobile
	 * @param content
	 * @return
	 */
	public static Ret sendCodeSms(String mobile, String content) {
		if(StringUtils.isEmpty(mobile)) {
			return Ret.fail("手机号不能为空");
		}
		SendSmsRtn rtn = sendSubmailSms(mobile, content);
		if(rtn.isStatus()) { // 成功
			saveSmsLog(mobile, content, rtn);
		} else {
			rtn = sendSubmailSms(mobile, content);
			saveSmsLog(mobile, content, rtn);
		}
		if(rtn.isStatus()) {
			return Ret.ok("发送成功");
		} else {
			return Ret.fail("验证码短信发送失败："+rtn.getMsg());
		}
	}
	
	/**
	 * 赛邮短信接口
	 * @param tenantOrgId
	 * @param mobile
	 * @param content
	 * @return
	 */
	private static SendSmsRtn sendSubmailSms(String mobile, String content) {
		try {
			String smsSign = "管店云";
			TenantOrg tenantOrg = TenantOrg.dao.findCacheById();
			if(tenantOrg != null && StringUtils.isNotEmpty(tenantOrg.getName())) {
				smsSign = tenantOrg.getName();
			}
			content = "【"+smsSign+"】"+content;
			
			Map<String, String> queryParas = new HashMap<>();
			queryParas.put("appid", CommonConfig.smsSubmailAppid);
			queryParas.put("to", mobile);
			queryParas.put("content", content);
			queryParas.put("signature", CommonConfig.smsSubmailAppkey);
			Map<String, String> headers = new HashMap<>();
			headers.put("Accept", "application/json");
			headers.put("Content-Type", "application/json");
			String ret = HttpKit.post(MYSUBMAIL_URL_API, JSONObject.toJSONString(queryParas), headers);
			JSONObject retJson = JSONObject.parseObject(ret);
			String status = retJson.getString("status");
			
			SendSmsRtn rtn = new SendSmsRtn();
			rtn.setStatus(StringUtils.equalsIgnoreCase(status, "success"));
			if(!rtn.isStatus()) {
				rtn.setMsg(retJson.getString("code")+":"+retJson.getString("msg"));
			}
			if(StringUtils.equalsIgnoreCase(status, "success")) {
				rtn.setFee(retJson.getInteger("fee"));
			} else {
				rtn.setFee(0);
			}
			return rtn;
		} catch (Exception e) {
			LOG.error("赛邮短信接口发送异常", e);
			
			SendSmsRtn rtn = new SendSmsRtn();
			rtn.setStatus(false);
			rtn.setMsg("短信接口异常");
			return rtn;
		}
	}

	/**
	 * 记录日志
	 * @param tenantOrgId
	 * @param mobile
	 * @param content
	 * @param retJson
	 */
	private static void saveSmsLog(String mobile, String content, SendSmsRtn rtn) {
		TenantSmsLog smsLog = new TenantSmsLog();
		smsLog.setContent(content);
		smsLog.setCreatedAt(new Date());
		smsLog.setMobile(mobile);
		if(rtn.isStatus()) { // 成功
			smsLog.setSendFlag(FlagEnum.YES.getValue());
			
		} else {
			smsLog.setSendFlag(FlagEnum.NO.getValue());
		}
		smsLog.setUserInfoId(0);
		smsLog.setSendDesc(rtn.getMsg());
		smsLog.save();
	}
	
	/**
	 * 发送通知短信
	 * @param tenantOrgId
	 * @param mobile
	 * @param content
	 * @return
	 */
	public static Ret sendNoticeSms(String mobile, String content) {
		if(StringUtils.isEmpty(mobile)) {
			return Ret.fail("手机号不能为空");
		}
		SendSmsRtn rtn = sendZhangjunSms(mobile, content);
		if(rtn.isStatus()) { // 发送成功
			saveSmsLog(mobile, content, rtn);
		} else {
			rtn = sendZhangjunSms(mobile, content);
			saveSmsLog(mobile, content, rtn);
		}
		if(rtn.isStatus()) {
			return Ret.ok();
		} else {
			return Ret.fail("通知短信发送失败："+rtn.getMsg());
		}
	}
	
	/**
	 * 骏媒短信接口
	 * @param tenantOrgId
	 * @param mobile
	 * @param content
	 * @return
	 */
	private static SendSmsRtn sendZhangjunSms(String mobile, String content) {
		try {
			String smsSign = "管店云";
			TenantOrg tenantOrg = TenantOrg.dao.findCacheById();
			if(tenantOrg != null && StringUtils.isNotEmpty(tenantOrg.getName())) {
				smsSign = tenantOrg.getName();
			}
			content = "【"+smsSign+"】"+content;
			
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("userid", CommonConfig.smsJunmeiUserId); // 企业id
			String timestamp = DateUtil.getSecondNumber(new Date());
			params.put("timestamp", timestamp);
			params.put("sign", MD5.create().digestHex(CommonConfig.smsJunmeiUserUser+CommonConfig.smsJunmeiUserPwd+timestamp)); // 账号+密码+时间戳 生成MD5字符串
			params.put("sendTime", "");
			params.put("action", "send");
			params.put("extno", "");
			params.put("content", content);
			params.put("mobile", mobile);
			
			String sendResult = HttpKit.get(ZHANGJUN_URL_API, params);
			XmlProtocolSupport xml = new XmlProtocolSupport();
			xml.setDocument(DocumentHelper.parseText(sendResult));
			String returnstatus = xml.getElementText("/returnsms/returnstatus", "fail");
			String message = xml.getElementText("/returnsms/message");
			
			SendSmsRtn rtn = new SendSmsRtn();
			rtn.setStatus(StringUtils.equalsIgnoreCase(returnstatus, "Success"));
			rtn.setMsg(message);
			if(StringUtils.equalsIgnoreCase(returnstatus, "Success")) {
				Integer counts = Integer.parseInt(xml.getElementText("/returnsms/successCounts"));
				rtn.setFee(counts);
			} else {
				rtn.setFee(0);
			}
			return rtn;
			
		} catch (Exception e) {
			LOG.error("掌骏短信接口发送异常", e);
			SendSmsRtn rtn = new SendSmsRtn();
			rtn.setStatus(false);
			rtn.setMsg("短信接口异常");
			return rtn;
		}
	}
	
}
/**
 * 短信接口返回消息
 */
class SendSmsRtn {
	private boolean status;
	private String msg;
	private int fee;
	
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public int getFee() {
		return fee;
	}
	public void setFee(int fee) {
		this.fee = fee;
	}
}
