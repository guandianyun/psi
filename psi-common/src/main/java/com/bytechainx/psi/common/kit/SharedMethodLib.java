/**
 * 本项目采用《咖啡授权协议》，保护知识产权，就是在保护我们自己身处的行业。
 * 
 * Copyright (c) 2011-2021, jfinal.com
 */

package com.bytechainx.psi.common.kit;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 用于 enjoy 模板引擎的 Shared Method 库
 */
public class SharedMethodLib {
	
	boolean devMode;
	Integer ver;
	
	public SharedMethodLib setDevMode(boolean devMode) {
		this.devMode = devMode;
		return this;
	}
	
	/**
	 * 设置 js、css 的版本号，用于部署环境
	 */
	public SharedMethodLib setVer(int ver) {
		this.ver = ver;
		return this;
	}
	
	/**
	 * 生成 js、css 的版本号，开发环境生成动态值用于消除浏览器缓存，从而实时看到效果
	 * 部署环境直接输出 setVer(...) 配置的值，便于浏览器缓存
	 * 
	 * 使用例子在 _admin_layout.html 中：
	 * <link rel="stylesheet" href="/assets/css/jfinal-blog-admin.css?v=#(ver())">
	 * <script src="/assets/js/jfinal-blog-admin.js?v=#(ver())"></script>
	 */
	public Integer ver() {
		return devMode ? ThreadLocalRandom.current().nextInt() : ver;
	}
}



