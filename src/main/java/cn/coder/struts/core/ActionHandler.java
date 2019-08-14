package cn.coder.struts.core;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

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
import cn.coder.struts.annotation.With;
import cn.coder.struts.aop.Aop;
import cn.coder.struts.support.ActionSupport;
import cn.coder.struts.util.BeanUtils;
import cn.coder.struts.util.ContextUtils;
import cn.coder.struts.wrapper.ResponseWrapper;

import static cn.coder.struts.util.ContextUtils.mergeInterceptor;

public final class ActionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ActionHandler.class);

	private HashMap<String, Action> mappings = new HashMap<>();
	private ResponseWrapper responseWrapper = new ResponseWrapper();

	public synchronized void init(Class<?>[] controllers, Class<?>[] interceptors, FilterRegistration registration) {
		// 增加全局Filter
		EnumSet<DispatcherType> dispatcherTypes = EnumSet.allOf(DispatcherType.class);
		dispatcherTypes.add(DispatcherType.REQUEST);
		dispatcherTypes.add(DispatcherType.FORWARD);

		for (Class<?> clazz : controllers) {
			mappingActions(clazz, interceptors, dispatcherTypes, registration);
		}
		if (logger.isDebugEnabled())
			logger.debug("Init {} actions", mappings.size());
	}

	private void mappingActions(Class<?> clazz, Class<?>[] interceptors, EnumSet<DispatcherType> dispatcherTypes,
			FilterRegistration registration) {
		Request req = clazz.getAnnotation(Request.class);
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			Request req2 = method.getAnnotation(Request.class);
			if (req2 != null) {
				String path = ContextUtils.genericPath(req, req2);
				if (logger.isDebugEnabled())
					logger.debug("Build action '{}' from '{}.{}'", path, clazz.getName(), method.getName());
				Action action = new Action(method);
				buildInterceptors(action, clazz, interceptors);
				mappings.put(path, action);
				registration.addMappingForUrlPatterns(dispatcherTypes, true, path);
			}
		}
	}

	private static void buildInterceptors(Action action, Class<?> controller, Class<?>[] interceptors) {
		Before before;
		// 如果跳过拦截器，拦截器全部清除，只保留本函数@Before的拦截器
		if (action.getMethod().getAnnotation(Skip.class) != null) {
			before = action.getMethod().getAnnotation(Before.class);
			if (before == null)
				action.setInterceptors(new Class<?>[0]);
			else
				action.setInterceptors(before.value());
		} else {
			// Action没有Skip，但是Controller有Skip注解，取Controller和Action自己的注解
			if (controller.getAnnotation(Skip.class) != null) {
				action.setInterceptors(mergeInterceptor(controller.getAnnotation(Before.class),
						action.getMethod().getAnnotation(Before.class)));
			} else {// 都没有Skip,设置全部的拦截器
				ArrayList<Class<?>> all = new ArrayList<>();
				for (Class<?> inter : interceptors) {
					With with = inter.getAnnotation(With.class);
					if (with != null && with.value().length > 0) {
						for (Class<? extends ActionSupport> target : with.value()) {
							if (controller.equals(target)) {
								all.add(inter);
							}
						}
					} else {
						all.add(inter);
					}
				}
				action.setInterceptors(BeanUtils.toArray(all));
			}
		}
	}

	public Action getAction(String path) {
		return mappings.get(path);
	}

	public void handle(Action action, HttpServletRequest req, HttpServletResponse res) throws IOException {
		if (!action.getHttpMethod().equals("ALL") && !req.getMethod().equals(action.getHttpMethod())) {
			if (logger.isDebugEnabled())
				logger.debug("Action '{}' not allowed '{}'", req.getServletPath(), req.getMethod());
			res.sendError(405);
			return;
		}
		Invocation inv = new Invocation(req, res, action);
		if (!inv.complete()) {
			if (logger.isDebugEnabled())
				logger.debug("Action stoped by interceptor '{}'", inv.current().getName());
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
			HttpServletResponse res) throws Exception {
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
