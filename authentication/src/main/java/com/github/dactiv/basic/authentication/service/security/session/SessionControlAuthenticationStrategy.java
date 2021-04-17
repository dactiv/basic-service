package com.github.dactiv.basic.authentication.service.security.session;

import com.github.dactiv.framework.spring.security.entity.AnonymousUser;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 自定义 session 控制认证策略实现
 *
 * @author maurice
 */
public class SessionControlAuthenticationStrategy extends ConcurrentSessionControlAuthenticationStrategy {

    private final SessionRegistry sessionRegistry;

    /**
     * @param sessionRegistry the session registry which should be updated when the
     *                        authenticated session is changed.
     */
    public SessionControlAuthenticationStrategy(SessionRegistry sessionRegistry) {
        super(sessionRegistry);
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void onAuthentication(Authentication authentication,
                                 HttpServletRequest request, HttpServletResponse response) {

        final List<SessionInformation> sessions;

        if (SecurityUserDetails.class.isAssignableFrom(authentication.getDetails().getClass())) {
            sessions = sessionRegistry.getAllSessions(authentication.getName(), false);
        } else if (AnonymousUser.class.isAssignableFrom(authentication.getDetails().getClass())) {
            return;
        } else {
            sessions = sessionRegistry.getAllSessions(authentication.getPrincipal(), false);
        }

        int sessionCount = sessions.size();
        int allowedSessions = getMaximumSessionsForThisUser(authentication);

        if (sessionCount < allowedSessions) {
            // They haven't got too many login sessions running at present
            return;
        }

        if (allowedSessions == -1) {
            // We permit unlimited logins
            return;
        }

        if (sessionCount == allowedSessions) {
            HttpSession session = request.getSession(false);

            if (session != null) {
                // Only permit it though if this request is associated with one of the
                // already registered sessions
                for (SessionInformation si : sessions) {
                    if (si.getSessionId().equals(session.getId())) {
                        return;
                    }
                }
            }
            // If the session is null, a new one will be created by the parent class,
            // exceeding the allowed number
        }

        allowableSessionsExceeded(sessions, allowedSessions, sessionRegistry);
    }

}
