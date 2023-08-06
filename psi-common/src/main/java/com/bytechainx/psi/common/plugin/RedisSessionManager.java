/**
 * 
 */
package com.bytechainx.psi.common.plugin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;

import io.undertow.UndertowMessages;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.SecureRandomSessionIdGenerator;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionIdGenerator;
import io.undertow.server.session.SessionListener;
import io.undertow.server.session.SessionListeners;
import io.undertow.server.session.SessionManager;
import io.undertow.server.session.SessionManagerStatistics;
import io.undertow.util.AttachmentKey;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

/**
 * A SessionManager that uses Redis to store session data.  Sessions are stored as a Redis Hash and sessions attributes
 * are stored directly in fields of that Hash
 *
 */
public class RedisSessionManager implements SessionManager {
    private final static String DEFAULT_DEPLOYMENT_NAME = "SESSION_MANAGER";
    private final static String CREATED_FIELD = ":created";

    private final AttachmentKey<SessionImpl> NEW_SESSION = AttachmentKey.create(SessionImpl.class);
    private final SessionIdGenerator sessionIdGenerator;
    private final String deploymentName;
    private final SessionListeners sessionListeners = new SessionListeners();
    private volatile int defaultSessionTimeout = 30 * 60;
    private final SessionConfig sessionConfig;
    private final JedisPool jedisPool;


    public RedisSessionManager(final URI redisUri, final SessionConfig sessionConfig) {
        this(DEFAULT_DEPLOYMENT_NAME, new SecureRandomSessionIdGenerator(), redisUri, sessionConfig);
    }

    public RedisSessionManager(final String deploymentName, final SessionIdGenerator sessionIdGenerator,
                               final URI redisUri, final SessionConfig sessionConfig) {
        this.deploymentName = deploymentName;
        this.sessionIdGenerator = sessionIdGenerator;
        this.sessionConfig = sessionConfig;

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPool = new JedisPool(jedisPoolConfig, redisUri);
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void start() {
       
    }

    public void stop() {
        jedisPool.destroy();
    }

    public Session createSession(final HttpServerExchange serverExchange, final SessionConfig sessionConfig) {
        if (sessionConfig == null) {
            throw UndertowMessages.MESSAGES.couldNotFindSessionCookieConfig();
        }
        String sessionId = sessionConfig.findSessionId(serverExchange);
        int count = 0;

        while (sessionId == null) {
            sessionId = sessionIdGenerator.createSessionId();
            try (Jedis jedis = jedisPool.getResource()) {
                if (jedis.exists(sessionId)) {
                    sessionId = null;
                }
            }
            if (count++ == 100) {
                //this should never happen
                //but we guard against pathological session id generators to prevent an infinite loop
                throw UndertowMessages.MESSAGES.couldNotGenerateUniqueSessionId();
            }
        }
        final long created = System.currentTimeMillis();
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(sessionId + CREATED_FIELD, String.valueOf(created));
        }
        final SessionImpl session = new SessionImpl(sessionId, created, defaultSessionTimeout,
                sessionConfig, this);

        sessionConfig.setSessionId(serverExchange, session.getId());
        session.bumpTimeout();
        sessionListeners.sessionCreated(session, serverExchange);
        serverExchange.putAttachment(NEW_SESSION, session);

        return session;
    }

    public Session getSession(final HttpServerExchange serverExchange, final SessionConfig sessionConfig) {
        if (serverExchange != null) {
            SessionImpl newSession = serverExchange.getAttachment(NEW_SESSION);
            if (newSession != null) {
                return newSession;
            }
        }
        String sessionId = sessionConfig.findSessionId(serverExchange);
        return getSession(sessionId);
    }

    public Session getSession(final String sessionId) {
        if (sessionId == null) {
            return null;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            if (jedis.exists(sessionId)) {
                long created = Long.valueOf(jedis.get(sessionId + CREATED_FIELD));
                int ttl = jedis.ttl(sessionId).intValue();
                return new SessionImpl(sessionId, created, ttl, sessionConfig, this);
            } else {
                return null;
            }
        }
    }

    public synchronized void registerSessionListener(final SessionListener listener) {
        sessionListeners.addSessionListener(listener);
    }

    public synchronized void removeSessionListener(final SessionListener listener) {
        sessionListeners.removeSessionListener(listener);
    }

    public void setDefaultSessionTimeout(final int timeout) {
        defaultSessionTimeout = timeout;
    }

    public Set<String> getTransientSessions() {
        // No sessions should be lost when shutting down a node
        return Collections.emptySet();
    }

    public Set<String> getActiveSessions() {
        return getAllSessions();
    }

    public Set<String> getAllSessions() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.keys("*");
        }
    }

    // TODO: support statistics
    public SessionManagerStatistics getStatistics() {
        return null;
    }

    private static class SessionImpl implements Session {
        private String sessionId;
        private final long creationTime;
        private volatile long maxInactiveInterval;
        private final SessionConfig sessionConfig;
        private final RedisSessionManager sessionManager;

        private SessionImpl(final String sessionId, final long creationTime,
                            final int maxInactiveInterval, final SessionConfig sessionConfig,
                            final RedisSessionManager sessionManager) {
            this.sessionId = sessionId;
            this.creationTime = creationTime;
            this.maxInactiveInterval = maxInactiveInterval;
            this.sessionConfig = sessionConfig;
            this.sessionManager = sessionManager;
        }

        public String getId() {
            return sessionId;
        }

        public void requestDone(HttpServerExchange serverExchange) {

        }

        public long getCreationTime() {
            return creationTime;
        }

        public long getLastAccessedTime() {
            try (Jedis jedis = sessionManager.jedisPool.getResource()) {
                return System.currentTimeMillis() - ((maxInactiveInterval * 100) - jedis.pttl(sessionId));
            }
        }

        public void setMaxInactiveInterval(final int interval) {
            maxInactiveInterval = interval;
            bumpTimeout();
        }

        public int getMaxInactiveInterval() {
            return (int)maxInactiveInterval;
        }

        public Object getAttribute(String name) {
            try (Jedis jedis = sessionManager.jedisPool.getResource()) {
                final String attribute = jedis.hget(sessionId, name);

                if (attribute == null) {
                    return null;
                }
                bumpTimeout();
                return deserialize(attribute);
            }
        }

        private Object deserialize(String data) {
            if (data == null) {
                return null;
            }
            byte[] attributeBytes = Base64.getDecoder().decode(data);
            try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(attributeBytes));
                 ObjectInputStream ois = new ObjectInputStream(bis)) {

                return ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public Set<String> getAttributeNames() {
            bumpTimeout();
            try (Jedis jedis = sessionManager.jedisPool.getResource()) {
                return jedis.hkeys(sessionId);
            }
        }

        @Override
        public Object setAttribute(String name, Object value) {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos))) {
                oos.writeObject(value);
                oos.flush();

                Object existing;
                try (Jedis jedis = sessionManager.jedisPool.getResource()) {
                    existing = deserialize(jedis.hget(sessionId, name));

                    jedis.hset(sessionId, name, Base64.getEncoder().encodeToString(bos.toByteArray()));
                }
                if (existing == null) {
                    sessionManager.sessionListeners.attributeAdded(this, name, value);
                } else {
                    sessionManager.sessionListeners.attributeUpdated(this, name, value, existing);
                }
                bumpTimeout();
                return value;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public Object removeAttribute(String name) {
            final Object existing = getAttribute(name);
            try (Jedis jedis = sessionManager.jedisPool.getResource()) {
                jedis.hdel(sessionId, name);
            }
            sessionManager.sessionListeners.attributeRemoved(this, name, existing);
            bumpTimeout();

            return existing;
        }

        @Override
        public void invalidate(HttpServerExchange exchange) {
            try (Jedis jedis = sessionManager.jedisPool.getResource()) {
                Transaction transaction = jedis.multi();
                transaction.del(sessionId);
                transaction.del(sessionId + CREATED_FIELD);
                transaction.exec();
            }

            if (exchange != null) {
                sessionConfig.clearSession(exchange, this.getId());
            }
        }

        @Override
        public SessionManager getSessionManager() {
            return sessionManager;
        }

        @Override
        public String changeSessionId(HttpServerExchange exchange, SessionConfig config) {
            final String oldId = sessionId;
            String newId = sessionManager.sessionIdGenerator.createSessionId();
            this.sessionId = newId;
            try (Jedis jedis = sessionManager.jedisPool.getResource()) {
                jedis.rename(oldId, newId);
            }
            config.setSessionId(exchange, this.getId());
            sessionManager.sessionListeners.sessionIdChanged(this, oldId);
            return newId;
        }

        private void bumpTimeout() {
            try (Jedis jedis = sessionManager.jedisPool.getResource()) {
                Transaction transaction = jedis.multi();
                transaction.expire(sessionId, maxInactiveInterval);
                transaction.expire(sessionId + CREATED_FIELD, maxInactiveInterval);

                transaction.exec();
            }
        }
    }
}