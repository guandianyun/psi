package com.bytechainx.psi.web.web.controller.account;

import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.FlagEnum;
import com.bytechainx.psi.common.EnumConstant.UserActiveStatusEnum;
import com.bytechainx.psi.common.dto.SmsCodeDto;
import com.bytechainx.psi.common.dto.UserSession;
import com.bytechainx.psi.common.kit.CipherkeyUtil;
import com.bytechainx.psi.common.kit.SmsKit;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TenantRole;
import com.bytechainx.psi.common.service.setting.TenantRoleService;
import com.bytechainx.psi.web.config.AppConfig;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.bytechainx.psi.web.web.interceptor.PermissionInterceptor;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;

import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.HttpStatus;

/**
 * 登录
 * @author defier
 *
 */
@Clear({PermissionInterceptor.class})
@Path("/")
public class LoginController extends BaseController {
	
	private static final Log LOG = Log.getLog(LoginController.class);
	
	@Inject
	private TenantRoleService roleService;
	
	public void index() {
		try {
			Integer adminId = getAdminId();
			if(adminId != null) { // 登录状态，但不是同一个租户，则退出
				render("dashboard/index.html");
				return;
			}
			removeSessionAttr(CommonConstant.SESSION_ID_KEY);
			
			setAttr("tenantOrg", getCurrentTenant());
			setAttr("version", AppConfig.version);
			
		} catch (Exception e) {
			LOG.error("加载租户登录界面异常", e);
			renderError(HttpStatus.HTTP_INTERNAL_ERROR);
		}
	}

	/**
	 * 登录
	 */
	public void login() {
		try {
	        String mobile = getPara("mobile");
	        String password = getPara("password");// 密码
	        String mac = getPara("mac");
	        
	        if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)) {
	            renderJson(Ret.fail("手机号或者密码不能为空"));
	            return;
	        }
	        
	        TenantAdmin admin = TenantAdmin.dao.findBy(mobile);
	        if (admin == null) {
	        	renderJson(Ret.fail("手机号或者密码不正确"));
	            return;
	        }
	        if(admin.getLoginFlag() == FlagEnum.NO.getValue()) {
	        	renderJson(Ret.fail("用户不可登录"));
	            return;
	        }
	        if(admin.getActiveStatus() == UserActiveStatusEnum.disable.getValue()) {
	        	renderJson(Ret.fail("用户已被禁用，无法登录"));
	            return;
	        }
	        if(StringUtils.isNotEmpty(admin.getMac()) && !StringUtils.equalsIgnoreCase(admin.getMac(), mac)) {
	        	renderJson(Ret.fail("当前电脑禁止登录系统"));
	            return;
	        }
	        
	        // 验证密码
	        String encodePassword = CipherkeyUtil.encodePassword(password, admin.getEncrypt());
	        if (!StringUtils.equals(admin.getPassword(), encodePassword)) {
	        	renderJson(Ret.fail("手机号或者密码不正确"));
	            return;
	        }
			// 登录数据换成分两个数据，一个是以用户ID为key，value为sessionid的，一个是以sessionid为key，value为usersession的两个数据
	        removeOnlineSession(CommonConstant.ONLINE_USER_ID_PC_CACHE, admin.getId());
			
			String ip = ServletUtil.getClientIP(getRequest());
			if(admin.getLoginCount() <= 0) {
				admin.setFirstLoginIp(ip);
				admin.setFirstLoginTime(new Date());
			}
			admin.setUpdatedAt(new Date());
			admin.setLastLoginIp(ip);
			admin.setLastLoginTime(new Date());
			admin.setLoginCount(admin.getLoginCount()+1);
			admin.update();
			
			TenantRole role = admin.getRole();
			UserSession session = new UserSession();
			session.setTenantAdminId(admin.getId());
			session.setSuperFlag(role.getSuperFlag());
			session.setHeartTime(new Date());
			session.setLoginIp(ip);
			session.setRealName(admin.getRealName());
			
			if(!role.getSuperFlag()) { // 非超级管理员，加载权限
				Set<String> operCodes = roleService.findOperByRoleId(admin.getRoleId());
				session.setOperCodeSet(operCodes);
			}
			// 用户放入redis
			addOnlineSession(CommonConstant.ONLINE_USER_ID_PC_CACHE, admin.getId(), getSession().getId());
			
			setSessionAttr(CommonConstant.SESSION_ID_KEY, session);
			
			LOG.info("用户登录成功, adminId:%s", admin.getId());
			
			if(admin.getActiveStatus() == UserActiveStatusEnum.waiting.getValue()) { // 待激活
				renderJson(Ret.ok("首次登录，进入帐户激活...").set("status", UserActiveStatusEnum.waiting.getValue()));
			} else {
				renderJson(Ret.ok("登录成功，欢迎使用『管店云』进销存系统..."));
			}
	        
		} catch (Exception e) {
			LOG.error("用户登录异常", e);
			renderJson(Ret.fail("用户登录异常:"+e.getMessage()));
		}
        
	}
	
	/**
	 * 忘记密码
	 */
	public void forgetPwd() {
		
	}
	
	/**
	 * 发送手机验证码
	 */
	public void sendSmsCode() {
		String mobile = getPara("mobile");
		if(StringUtils.isEmpty(mobile)) {
			renderJson(Ret.fail("手机号不能为空"));
			return;
		}
		TenantAdmin admin = TenantAdmin.dao.findBy(mobile);
        if (admin == null) {
        	renderJson(Ret.fail("手机号输入错误"));
            return;
        }
		Ret ret = SmsKit.forgetPwd(mobile);
		if(ret.isOk()) {
			setSessionAttr(CommonConstant.SESSION_SMS_CODE, new SmsCodeDto(ret.getStr("smsCode"), new Date(), mobile));
		}
		renderJson(ret);
	}
	
	/**
	 * 更新密码
	 */
	public void updatePwd() {
		try {
			String mobile = getPara("mobile");
			if (StringUtils.isEmpty(mobile)) {
				renderJson(Ret.fail("手机号不能为空"));
				return;
			}
			String smsCode = getPara("smsCode");
			if (StringUtils.isEmpty(smsCode)) {
				renderJson(Ret.fail("验证码不能为空"));
				return;
			}
			SmsCodeDto smsCodeDto = (SmsCodeDto) getSessionAttr(CommonConstant.SESSION_SMS_CODE);
			if (smsCodeDto == null) {
				renderJson(Ret.fail("无效的短信验证码"));
				return;
			}
			long currentTime = System.currentTimeMillis();
			long sendTime = smsCodeDto.getSendTime().getTime();
			boolean isOutTime = currentTime - sendTime > 5 * 60 * 1000; // 5分钟超时
			if (isOutTime) {
				removeSessionAttr(CommonConstant.SESSION_SMS_CODE);
				renderJson(Ret.fail("短信验证码超时"));
				return;
			}
			if (!StringUtils.equals(smsCode, smsCodeDto.getCode())) {
				renderJson(Ret.fail("短信验证码输入不正确"));
				return;
			}
			String newPwd = getPara("newPwd");
			if (StringUtils.isEmpty(newPwd)) {
				renderJson(Ret.fail("新密码不能为空"));
				return;
			}
			TenantAdmin admin = TenantAdmin.dao.findBy(mobile);
	        if (admin == null) {
	        	renderJson(Ret.fail("手机号输入错误"));
	            return;
	        }
			removeSessionAttr(CommonConstant.SESSION_SMS_CODE);
			
			String encodePassword = CipherkeyUtil.encodePassword(newPwd, admin.getEncrypt());
			admin.setPassword(encodePassword);
			admin.setUpdatedAt(new Date());
			admin.update();
			
			renderJson(Ret.ok("修改密码成功"));
		} catch (Exception e) {
			LOG.error("修改用户密码异常", e);
			renderJson(Ret.fail("修改用户密码异常:"+e.getMessage()));
		}
		
	}
	
}
