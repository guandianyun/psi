package com.bytechainx.psi.web;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import com.bytechainx.psi.common.plugin.RedisSessionManagerFactory;
import com.bytechainx.psi.web.config.AppConfig;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.server.undertow.UndertowServer;

import redis.clients.jedis.Protocol;

/**
 * 启动入口
 */
public class App {
	
	public static void main(String[] args) {
		
		UndertowServer undertowServer = UndertowServer.create(AppConfig.class);
		
		undertowServer.configWeb(builder -> {
			try {
				Prop prop = PropKit.useFirstFound("app-config-pro.txt", "app-config-dev.txt");
				Prop commonProp = PropKit.append(new File(prop.get("common.config.path")+File.separator+"app-config-dev.txt"));
				
				String redisHost = commonProp.get("redis.ip");
				int redisPort = commonProp.getInt("redis.port");
				int redisDb = Protocol.DEFAULT_DATABASE;
				
				String redisUri = "redis://"+redisHost+":"+redisPort+"/"+redisDb;
				builder.getDeploymentInfo().setSessionManagerFactory(new RedisSessionManagerFactory(new URI(redisUri)));
				
			} catch (URISyntaxException e) {
				e.printStackTrace();
				System.exit(-1);
			}
            // 配置WebSocket需使用ServerEndpoint注解
//            builder.addWebSocketEndpoint("cn.york.common.websocket.WebSocketEndpoint");
        });
		undertowServer.start();
	}

}





