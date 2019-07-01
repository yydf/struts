package cn.coder.struts.wrapper;

import java.util.HashMap;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionWrapper implements HttpSessionListener {
	private static final Logger logger = LoggerFactory.getLogger(SessionWrapper.class);
	
	private static final HashMap<String, HttpSession> sessionList = new HashMap<>();
	private static final Object obj = new Object();

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		synchronized (obj) {
			sessionList.put(session.getId(), session);
			if (logger.isDebugEnabled())
				logger.debug("Session created:{}", session.getId());
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		synchronized (obj) {
			sessionList.remove(session.getId());
			if (logger.isDebugEnabled())
				logger.debug("Session destroyed:{}", session.getId());
		}
	}

	public static Object getAttribute(String name, String sId) {
		HttpSession session = sessionList.get(sId);
		if (session != null) {
			if (logger.isDebugEnabled())
				logger.debug("Find the session:{}", sId);
			return session.getAttribute(name);
		}
		return null;
	}

}
