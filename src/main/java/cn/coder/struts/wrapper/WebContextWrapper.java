package cn.coder.struts.wrapper;

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
import cn.coder.struts.annotation.StartUp;
import cn.coder.struts.jdbc.SqlSessionBase;
import cn.coder.struts.support.ActionIntercepter;
import cn.coder.struts.util.StrutsUtils;
import cn.coder.struts.util.StrutsUtils.FilterClassType;

public class WebContextWrapper implements FilterClassType {

	private static final Logger logger = LoggerFactory.getLogger(WebContextWrapper.class);
	private ActionWrapper actionWrapper;
	private final HashMap<Class<?>, Object> classes = new HashMap<>();
	private final ArrayList<ActionIntercepter> filters = new ArrayList<>();

	public void init(ServletContext ctx, ActionWrapper wrapper) throws ServletException {
		long start = System.nanoTime();
		this.actionWrapper = wrapper;
		StrutsUtils.scanClasses(ctx, "/", this);
		createSession();
		registerAction(ctx.getFilterRegistration("StrutsFilter"));
		logger.debug("Init context:" + (System.nanoTime() - start) + "ns");
	}

	private void createSession() {
		try {
			InputStream input = WebContextWrapper.class.getClassLoader().getResourceAsStream("jdbc.properties");
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
		Set<String> mappedUrls = actionWrapper.getMappedUrls();
		if (!mappedUrls.isEmpty()) {
			EnumSet<DispatcherType> dispatcherTypes = EnumSet.allOf(DispatcherType.class);
			dispatcherTypes.add(DispatcherType.REQUEST);
			dispatcherTypes.add(DispatcherType.FORWARD);
			for (String action : mappedUrls) {
				filterRegistration.addMappingForUrlPatterns(dispatcherTypes, true, action);
				actionWrapper.registerBean(action, classes);
			}
		}
		// 执行启动类
		actionWrapper.runStartUp(classes);
		// 清除缓存
		classes.clear();
		logger.debug("Registered actions " + mappedUrls.size());
	}

	@Override
	public void filter(Class<?> clazz) {
		if (clazz != null) {
			if (StrutsUtils.isController(clazz)) {
				bindActions(clazz);
			} else if (StrutsUtils.isFilter(clazz)) {
				bindFilter(clazz);
			}
			classes.put(clazz, null);
		}
	}

	private void bindFilter(Class<?> clazz) {
		try {
			filters.add((ActionIntercepter) clazz.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("Instance intercepter faild", e);
		}
	}

	private void bindActions(Class<?> clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		Request classReq = clazz.getAnnotation(Request.class);
		Request methodReq;
		StartUp startUp;
		for (Method method : methods) {
			methodReq = method.getAnnotation(Request.class);
			if (methodReq != null) {
				actionWrapper.put(StrutsUtils.getUrlMapping(classReq, methodReq.value()), method);
			}
			startUp = method.getAnnotation(StartUp.class);
			if (startUp != null) {
				actionWrapper.add(method);
			}
		}
	}

	public void destroy() {
		long start = System.nanoTime();
		classes.clear();
		actionWrapper.clear();
		actionWrapper = null;
		filters.clear();
		SqlSessionBase.destory();
		logger.debug("Destroy context:" + (System.nanoTime() - start) + "ns");
	}

	public boolean checkFilter(HttpServletRequest req, HttpServletResponse res) {
		if (!filters.isEmpty()) {
			for (ActionIntercepter intercepter : filters) {
				if (!intercepter.intercept(req, res))
					return false;
			}
		}
		return true;
	}

}
