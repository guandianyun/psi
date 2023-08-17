package com.bytechainx.psi.web;

import com.bytechainx.psi.web.config.AppConfig;
import com.jfinal.server.undertow.UndertowServer;

/**
 * 启动入口
 */
public class App {
	
	public static void main(String[] args) {
		UndertowServer.start(AppConfig.class);
	}

}





