package cn.coder.struts.wrapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
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
import cn.coder.struts.support.ActionIntercepter;
import cn.coder.struts.util.ClassUtils;
import cn.coder.struts.util.ClassUtils.FilterClassType;

public class WebContextWrapper implements FilterClassType {

	private static final Logger logger = LoggerFactory.getLogger(WebContextWrapper.class);
	private ActionWrapper actionWrapper;
	private final HashMap<Class<?>, Object> classes = new HashMap<>();
	private final ArrayList<ActionIntercepter> filters = new ArrayList<>();

	public void init(ServletContext ctx, ActionWrapper wrapper) throws ServletException {
		long start = System.nanoTime();
		this.actionWrapper = wrapper;
		ClassUtils.scanClasses(ctx, "/", this);
		registerAction(ctx.getFilterRegistration("StrutsFilter"));
		logger.debug("Init context:" + (System.nanoTime() - start) + "ns");
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
			if (ClassUtils.isController(clazz)) {
				bindActions(clazz);
			} else if (ClassUtils.isFilter(clazz)) {
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
				actionWrapper.put(ClassUtils.getUrlMapping(classReq, methodReq.value()), method);
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
