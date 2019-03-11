package cn.coder.struts.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.annotation.Request;
import cn.coder.struts.core.ContextUtils.FilterClassType;
import cn.coder.struts.jdbc.SqlSessionBase;
import cn.coder.struts.support.ActionIntercepter;

public class StrutsContext implements FilterClassType {

	static final Logger logger = LoggerFactory.getLogger(StrutsContext.class);
	private final HashMap<String, ActionMapper> actionMap = new HashMap<>();
	private final ArrayList<Class<?>> classes = new ArrayList<>();
	private final ArrayList<ActionIntercepter> filters = new ArrayList<>();

	public void init(ServletContext ctx) throws ServletException {
		long start = System.nanoTime();
		ContextUtils.scanClasses(ctx, "/", this);
		registerAction(ctx.getFilterRegistration("StrutsFilter"));
		createSession();
		logger.debug("Init context:" + (System.nanoTime() - start) + "ns");
	}

	private void createSession() {
		try {
			InputStream input = StrutsContext.class.getClassLoader().getResourceAsStream("jdbc.properties");
			if (input != null) {
				logger.debug("Find the jdbc.properties file");
				Properties properties = new Properties();
				properties.load(input);
				SqlSessionBase.createSession(properties);
			}
		} catch (IOException | SQLException e) {
			logger.error("Create session faild", e);
		}
	}

	private void registerAction(FilterRegistration filterRegistration) throws ServletException {
		if (!actionMap.isEmpty()) {
			EnumSet<DispatcherType> dispatcherTypes = EnumSet.allOf(DispatcherType.class);
			dispatcherTypes.add(DispatcherType.REQUEST);
			dispatcherTypes.add(DispatcherType.FORWARD);
			Set<String> actions = actionMap.keySet();
			for (String action : actions) {
				filterRegistration.addMappingForUrlPatterns(dispatcherTypes, true, action);
				actionMap.get(action).createBean(classes);
			}
			logger.debug("Registered actions " + actions.size());
		}
	}

	@Override
	public void filter(Class<?> clazz) {
		if (clazz != null) {
			if (ContextUtils.isController(clazz)) {
				bindActions(clazz);
			}
			else if (ContextUtils.isFilter(clazz)) {
				bindFilter(clazz);
			}
			classes.add(clazz);
		}
	}

	private void bindFilter(Class<?> clazz) {
		try {
			filters.add((ActionIntercepter)clazz.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("Instance intercepter faild", e);
		}
	}

	private void bindActions(Class<?> clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		Request classReq = clazz.getAnnotation(Request.class);
		Request methodReq;
		for (Method method : methods) {
			methodReq = method.getAnnotation(Request.class);
			if (methodReq != null) {
				actionMap.put(ContextUtils.getUrlMapping(classReq, methodReq.value()), new ActionMapper(method));
			}
		}
	}

	public void destroy() {
		long start = System.nanoTime();
		classes.clear();
		actionMap.clear();
		filters.clear();
		SqlSessionBase.destory();
		logger.debug("Destroy context:" + (System.nanoTime() - start) + "ns");
	}

	public ActionMapper findAction(String servletPath) {
		return actionMap.get(servletPath);
	}

	public boolean checkFilter(HttpServletRequest req, HttpServletResponse res) {
		if(!filters.isEmpty()){
			for (ActionIntercepter intercepter : filters) {
				if(!intercepter.intercept(req, res))
					return false;
			}
		}
		return true;
	}

}
