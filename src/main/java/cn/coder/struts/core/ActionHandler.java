package cn.coder.struts.core;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.annotation.Before;
import cn.coder.struts.annotation.Param;
import cn.coder.struts.annotation.Request;
import cn.coder.struts.annotation.Skip;
import cn.coder.struts.aop.Aop;
import cn.coder.struts.support.ActionSupport;
import cn.coder.struts.wrapper.ResponseWrapper;

public final class ActionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ActionHandler.class);

	private HashMap<String, Action> mappings = new HashMap<>();
	private ResponseWrapper responseWrapper = new ResponseWrapper();

	public ActionHandler(List<Class<?>> controllers) {
		if (controllers != null && !controllers.isEmpty()) {
			for (Class<?> clazz : controllers) {
				mappingActions(clazz);
			}
		}
	}

	private void mappingActions(Class<?> clazz) {
		Request req = clazz.getAnnotation(Request.class);
		Method[] methods = clazz.getDeclaredMethods();
		if (methods.length > 0) {
			Request req2;
			for (Method method : methods) {
				req2 = method.getAnnotation(Request.class);
				if (req2 != null) {
					mappings.put(genericPath(req, req2), new Action(method));
				}
			}
		}
	}

	private static String genericPath(Request req, Request req2) {
		String path = req2.value().startsWith("/") ? req2.value() : "/" + req2.value();
		if (path.startsWith("~"))
			return path.substring(1);
		if (req == null)
			return path;
		String base = req.value().startsWith("/") ? req.value() : "/" + req.value();
		return base + path;
	}

	public void buildInterceptors(List<Class<?>> interceptors) {
		Class<?>[] all = new Class<?>[interceptors.size()];
		interceptors.toArray(all);
		Collection<Action> actions = mappings.values();
		Before before;
		for (Action action : actions) {
			// 如果跳过拦截器，拦截器全部清除，只保留本函数@Before的拦截器
			if (action.getMethod().getAnnotation(Skip.class) != null) {
				before = action.getMethod().getAnnotation(Before.class);
				if (before == null)
					action.setInterceptors(new Class<?>[0]);
				else
					action.setInterceptors(before.value());
			} else {
				// Action没有Skip，但是Controller有Skip注解，取Controller和Action自己的注解
				if (action.getController().getAnnotation(Skip.class) != null) {
					action.setInterceptors(merge(action.getController().getAnnotation(Before.class),
							action.getMethod().getAnnotation(Before.class)));
				} else {// 都没有Skip,设置全部的拦截器
					action.setInterceptors(all);
				}
			}
		}
	}

	private static Class<?>[] merge(Before b1, Before b2) {
		if (b1 == null && b2 == null)
			return new Class<?>[0];
		if (b1 == null || b1.value().length == 0)
			return b2.value();
		if (b2 == null || b2.value().length == 0)
			return b1.value();
		Class<?>[] arr1 = b1.value();
		Class<?>[] arr2 = b2.value();
		Class<?>[] arr = new Class<?>[arr1.length + arr2.length];
		System.arraycopy(arr1, 0, arr, 0, arr1.length);
		System.arraycopy(arr2, 0, arr, arr1.length, arr2.length);
		return arr;
	}

	public void registerPath(FilterRegistration filterRegistration) {
		Set<String> paths = mappings.keySet();
		if (!paths.isEmpty()) {
			// 增加全局Filter
			EnumSet<DispatcherType> dispatcherTypes = EnumSet.allOf(DispatcherType.class);
			dispatcherTypes.add(DispatcherType.REQUEST);
			dispatcherTypes.add(DispatcherType.FORWARD);
			for (String path : paths) {
				filterRegistration.addMappingForUrlPatterns(dispatcherTypes, true, path);
			}
		}
	}

	public Action getAction(String path) {
		return mappings.get(path);
	}

	public void handle(Action action, HttpServletRequest req, HttpServletResponse res) throws IOException {
		if (!req.getMethod().equals(action.getHttpMethod())) {
			if (logger.isDebugEnabled())
				logger.debug("Action '{}' not allowed '{}'", req.getServletPath(), req.getMethod());
			res.sendError(405);
			return;
		}
		Invocation invocation = new Invocation(req, res, action);
		if (!invocation.complete()) {
			if (logger.isDebugEnabled())
				logger.debug("Action stoped by interceptor");
			return;
		}
		ActionSupport support = null;
		try {
			support = (ActionSupport) Aop.create(action.getController());
			support.init(req, res);
			Object[] args = buildArgs(action, support, req, res);
			Object result = action.getMethod().invoke(support, args);
			if (result != null) {
				responseWrapper.doResponse(result, req, res);
			}
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error("Handle action faild", e);
		} finally {
			if (support != null) {
				support.clear();
			}
		}
	}

	private static Object[] buildArgs(Action action, ActionSupport support, HttpServletRequest req,
			HttpServletResponse res) {
		Parameter[] parameters = action.getParameters();
		Object[] args = null;
		if (parameters != null && parameters.length > 0) {
			args = new Object[parameters.length];
			Param p;
			for (int i = 0; i < parameters.length; i++) {
				p = parameters[i].getAnnotation(Param.class);
				if (p != null) {
					args[i] = support.getParameter(parameters[i].getType(), p.value());
				} else {
					if (parameters[i].getType().isAssignableFrom(HttpServletRequest.class)) {
						args[i] = req;
					} else if (parameters[i].getType().isAssignableFrom(HttpServletResponse.class)) {
						args[i] = res;
					} else if (parameters[i].getType().isAssignableFrom(HttpSession.class)) {
						args[i] = req.getSession();
					}
				}
			}
		}
		return args;
	}

	public synchronized void clear() {
		mappings.clear();
	}

}
