package com.bytechainx.psi.web.web.interceptor;

import java.lang.reflect.Method;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.dto.UserSession;
import com.bytechainx.psi.web.config.AppConfig;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.kit.Ret;

/**
 * 需要登录权限才能访问
 * 
 * @author defier
 *
 */
public class PermissionInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation inv) {
		BaseController controller = (BaseController) inv.getController();
		// 判断是否登录
		UserSession session = controller.getSessionAttr(CommonConstant.SESSION_ID_KEY);
		if (session == null) {
			redirectLogin(controller);
			return;
		}
		// 判断租户是否到期
		Integer tenantId = controller.getSessionAttr(CommonConstant.SESSION_TENANT_ID);
		if(tenantId == null) {
			redirectLogin(controller);
			return;
		}
		// 判断是否有权限
		Permissions[] operCodes = {};
		Method method = inv.getMethod();
		Permission permission = method.getAnnotation(Permission.class);
		if (permission != null && permission.value() != null) {
			operCodes = permission.value();
		}
		if(!session.hasAnyOper(operCodes)) { // 无权限
			forward(controller);
			return;
		}
		
		inv.invoke();
	}

	/**
	 * @param controller
	 */
	public void redirectLogin(BaseController controller) {
		String url = controller.getCookie(CommonConstant.COOKIE_TENANT_URL_CODE);
		if(controller.isJsonRequest()) {
			controller.renderJson(Ret.fail("登录超时，请重新登录").set("loginUrl", url));
		} else if (controller.isAjaxHtmlRequest()) {
			controller.render(AppConfig.noLoginViews);
		} else {
			controller.redirect(url);
		}
	}
	
	private void forward(BaseController controller) {
		if (controller.isJsonRequest()) {
			controller.renderJson(Ret.fail("没有操作权限"));
		} else if (controller.isAjaxHtmlRequest()) {
			controller.render(AppConfig.noPermissionViews);
		} else { // 正常的http请求
			controller.renderHtml("<script>alert('没有操作权限');location.href='/account/logout';</script>");
		}
	}

}
