package com.bytechainx.psi.web.web.interceptor;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.model.TenantOrg;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

/**
 * 一些公共数据，在此类中处理
 * @author defier
 *
 */
public class ViewContextInterceptor implements Interceptor {
	
//	private final static String TOKEN_NAME = "_mvc_token";

	@Override
	public void intercept(Invocation ai) {
		Controller controller = ai.getController();
		controller.setAttr("session", controller.getSessionAttr(CommonConstant.SESSION_ID_KEY));
		TenantOrg currentTenant = TenantOrg.dao.findCacheById();
		controller.setAttr("currentTenant", currentTenant);
		/**
		String methodName = ai.getMethodName();
		if (StringUtils.contains(methodName, "add") || StringUtils.contains(methodName, "edit")) {
			controller.createToken(TOKEN_NAME);
		}
		if (StringUtils.contains(methodName, "create") || StringUtils.contains(methodName, "update")) {
			if(!controller.validateToken(TOKEN_NAME)) {
				controller.renderJson(Ret.fail("操作过于频繁"));
				return;
			}
		}
		**/
		
		ai.invoke();
	}

}
