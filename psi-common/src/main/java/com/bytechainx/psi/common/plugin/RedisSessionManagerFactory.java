/**
 * 
 */
package com.bytechainx.psi.common.plugin;

import java.net.URI;

import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.SessionManagerFactory;

/**
 * @author defier
 *
 */
public class RedisSessionManagerFactory implements SessionManagerFactory {
	
	private URI redisUri;
	
	public RedisSessionManagerFactory(URI redisUri) {
		this.redisUri = redisUri;
	}
 
	@Override
	public SessionManager createSessionManager(Deployment arg0) {
		SessionCookieConfig sessionConfig = new SessionCookieConfig();
        sessionConfig.setCookieName(SessionCookieConfig.DEFAULT_SESSION_ID);
		return new RedisSessionManager(redisUri, sessionConfig);
	}
 
}