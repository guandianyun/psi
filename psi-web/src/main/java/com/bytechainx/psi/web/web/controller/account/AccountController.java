package com.bytechainx.psi.web.web.controller.account;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.UserActiveStatusEnum;
import com.bytechainx.psi.common.dto.SmsCodeDto;
import com.bytechainx.psi.common.kit.CipherkeyUtil;
import com.bytechainx.psi.common.kit.SmsKit;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.service.setting.TenantAdminService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.bytechainx.psi.web.web.interceptor.PermissionInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * 帐户登录
 */

@Path("/account")
public class AccountController extends BaseController {
	
	private static final Log LOG = Log.getLog(AccountController.class);
	
	@Inject
	private TenantAdminService tenantAdminService;
	
	/**
	 * 查看账户
	 */
	public void edit() {
		TenantAdmin currentAdmin = getCurrentAdmin();
		currentAdmin.remove("password", "encrypt");
		setAttr("account", currentAdmin);
	}
	
	/**
	* 修改
	*/
	@Before(Tx.class)
	public void update() {
		String realName = get("real_name");
		String mobile = get("mobile");
		if(StringUtils.isEmpty(realName)) {
			renderJson(Ret.fail("真实姓名不能为空"));
		}
		if(StringUtils.isEmpty(mobile)) {
			renderJson(Ret.fail("手机号不能为空"));
		}
		TenantAdmin currentAdmin = getCurrentAdmin();
		currentAdmin.setRealName(realName);
		currentAdmin.setMobile(mobile);
		Ret ret = tenantAdminService.update(currentAdmin);
		if(ret.isOk()) {
			getUserSession().setRealName(realName);
		}
		
		renderJson(ret);
	}
	
	/**
	 * 修改密码
	 */
	public void editPwd() {
		
	}
	
	/**
	 * 修改密码
	 */
	public void updatePwd() {
		Ret ret = tenantAdminService.updatePwd(getAdminId(), getPara("old_password"), getPara("new_password"));
		renderJson(ret);
	}
	
	/**
	 * 退出
	 */
	@Clear(PermissionInterceptor.class)
	public void logout() {
		removeSessionAttr(CommonConstant.SESSION_ID_KEY);
		String url = getCookie(CommonConstant.COOKIE_TENANT_URL_CODE);
		redirect(url);
	}
	
	
	/**
	 * 激活帐户
	 */
	public void activeIndex() {
		TenantAdmin currentAdmin = getCurrentAdmin();
		currentAdmin.remove("password", "encrypt");
		setAttr("currentAdmin", currentAdmin);
	}
	
	/**
	 * 激活帐户
	 */
	public void active() {
		try {
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
			removeSessionAttr(CommonConstant.SESSION_SMS_CODE);
			
			TenantAdmin admin = TenantAdmin.dao.findById(getAdminId());
			String encodePassword = CipherkeyUtil.encodePassword(newPwd, admin.getEncrypt());
			admin.setActiveStatus(UserActiveStatusEnum.enable.getValue());
			admin.setPassword(encodePassword);
			admin.setUpdatedAt(new Date());
			admin.update();
			
			renderJson(Ret.ok("激活帐户成功"));
			
		} catch (Exception e) {
			LOG.error("激活帐户异常", e);
			renderJson(Ret.fail("激活帐户异常:"+e.getMessage()));
		}
	}
	
	
	/**
	 * 发送手机验证码
	 */
	public void sendActiveCode() {
		TenantAdmin admin = getCurrentAdmin();
        if (admin == null) {
        	renderJson(Ret.fail("手机号输入错误"));
            return;
        }
		Ret ret = SmsKit.activeCode(admin.getMobile());
		if(ret.isOk()) {
			setSessionAttr(CommonConstant.SESSION_SMS_CODE, new SmsCodeDto(ret.getStr("smsCode"), new Date(), admin.getMobile()));
		}
		renderJson(ret);
	}
	
}
