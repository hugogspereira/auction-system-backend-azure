package scc.auth;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import scc.cache.RedisCache;

public class AuthSession {

    private RedisCache cache = RedisCache.getInstance();
    private static AuthSession instance;

    public static synchronized AuthSession getInstance() {
        if (instance == null) {
            instance = new AuthSession();
        }
        return instance;
    }

    public String checkSession(Cookie cookie, String sessionId) {
        if (cookie == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        String session = cookie.getValue();
        System.out.println("Session: " + session);
        if (session == null || !cache.existSession(session) || !cache.getSession(session).equals(sessionId)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return session;
    }

    public void putSession(String sessionId, String userNickname) {
        cache.putSession(sessionId, userNickname);
    }

    public String getSession(Cookie session) {
        if (session == null || session.getValue() == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return cache.getSession(session.getValue());
    }

    public void deleteSession(Cookie session) {
        cache.deleteSession(session.getValue());
    }

}
