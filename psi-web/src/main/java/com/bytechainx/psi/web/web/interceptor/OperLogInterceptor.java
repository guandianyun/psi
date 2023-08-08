package com.bytechainx.psi.web.web.interceptor;

import java.lang.reflect.Method;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.OperLogTypeEnum;
import com.bytechainx.psi.common.EnumConstant.PlatformTypeEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TenantOperLog;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

import cn.hutool.extra.servlet.ServletUtil;

/**
 * 操作日志拦截器，统一在这里记录日志
 * 
 * @author defier
 *
 */
public class OperLogInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation ai) {
		ai.invoke();
		// ACTION执行后记录日志，多线程记录，提升访问性能
		new Thread(() -> writeOperLog(ai)).start();
	}

	/**
	 * 记录操作日志
	 * @param ai
	 */
	private void writeOperLog(Invocation ai) {
		BaseController controller = (BaseController) ai.getController();
		String methodName = ai.getMethodName();
		TenantAdmin admin = null;
		Integer logType = null;
		if (StringUtils.contains(methodName, "delete")) {
			logType = OperLogTypeEnum.delete.getValue();

		} else if (StringUtils.contains(methodName, "create")) {
			logType = OperLogTypeEnum.create.getValue();
			
		} else if (StringUtils.contains(methodName, "update")) {
			logType = OperLogTypeEnum.update.getValue();

		} else if (StringUtils.contains(methodName, "disable")) {
			logType = OperLogTypeEnum.setting.getValue();
			
		} else if (StringUtils.contains(methodName, "enable")) {
			logType = OperLogTypeEnum.setting.getValue();
			
		} else if (StringUtils.contains(methodName, "close")) {
			logType = OperLogTypeEnum.setting.getValue();
			
		} else if (StringUtils.contains(methodName, "audit")) {
			logType = OperLogTypeEnum.setting.getValue();
			
		} else if (StringUtils.contains(methodName, "login")) {
			logType = OperLogTypeEnum.login.getValue();
			admin = TenantAdmin.dao.findBy(controller.getPara("mobile"));
			
		} else if (StringUtils.contains(methodName, "logout")) {
			logType = OperLogTypeEnum.login.getValue();
		}
		// 没有日志类型，则不记录
		if(logType == null) {
			return;
		}
		if (admin == null) {
			admin = controller.getCurrentAdmin();
		}
		if (admin == null) {
			return;
		}
		Permissions permissionCode = null;
		Method method = ai.getMethod();
		Permission permission = method.getAnnotation(Permission.class);
		if (permission != null && permission.value() != null) {
			permissionCode = permission.value()[0];
		}
		StringBuffer operDesc = new StringBuffer(); // 操作描述
		if(permissionCode != null) {
			Permissions parentPermission = Permissions.getEnum(permissionCode.name().substring(0, permissionCode.name().lastIndexOf("_")));
			if(StringUtils.contains(methodName, "enable")) { // 在权限里面的名称不管是启用还是停用，都是停用，使用的同一个权限，这里特殊判断下。
				operDesc.append(parentPermission.getName()+":启用");
			} else {
				operDesc.append(parentPermission.getName()+":"+permissionCode.getName());
			}
			String dataId = controller.getPara("id") == null ? controller.getPara("ids") : controller.getPara("id");; // 修改删除等数据的ID
			if(StringUtils.isNotEmpty(dataId)) {
				operDesc.append(", id:"+dataId);
			}
		}
		if(StringUtils.contains(methodName, "login")) {
			operDesc.append("登录");
		} else if(StringUtils.contains(methodName, "logout")) {
			operDesc.append("退出");
		}
		TenantOperLog log = new TenantOperLog();
		log.setLogType(logType);
		log.setOperPerson(admin.getRealName());
		log.setTenantAdminId(admin.getId());
		log.setOperDesc(operDesc.toString());
		log.setOperTime(new Date());
		log.setPlatformType(PlatformTypeEnum.web.getValue());
		log.setOperIp(ServletUtil.getClientIP(controller.getRequest()));
		log.save();
	}

}
