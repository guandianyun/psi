package com.bytechainx.psi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.bytechainx.psi.common.Permissions;

/**
 * 权限点注解，标注在action上面
 * @author defier.lai
 * @version 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Permission {
	/**
	 * 操作代码
	 * @return
	 */
	public abstract Permissions[] value();
}