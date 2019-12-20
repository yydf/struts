package cn.coder.struts.handler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.annotation.Param;
import cn.coder.struts.annotation.Request;
import cn.coder.struts.annotation.Skip;
import cn.coder.struts.core.ApplicationContext;
import cn.coder.struts.support.Controller;
import cn.coder.struts.support.HandlerInterceptor;
import cn.coder.struts.support.ServletWebRequest;
import cn.coder.struts.util.BeanUtils;
import cn.coder.struts.wrapper.OrderWrapper;

public class SimpleRequestHandler implements Handler {
	private static final Logger logger = LoggerFactory.getLogger(SimpleRequestHandler.class);

	private final ApplicationContext context;
	private final Map<String, HandlerMethod> handlerMethods;
	private HandlerInterceptor[] interceptors;

	public SimpleRequestHandler(ApplicationContext context) {
		this.context = context;
		this.handlerMethods = new HashMap<>();
		detectHandlers(context.getClasses());
		detectInterceptor(context.getClasses());
	}

	@Override
	public boolean lookup(ServletWebRequest req) {
		return getHandlerMethod(req) != null;
	}

	@Override
	public boolean preHandle(ServletWebRequest req) {
		HandlerMethod hm = getHandlerMethod(req);
		if (hm.getSkip()) {
			logger.debug("Skip all Interceptors preHandle.");
			return true;
		}
		if (interceptors.length > 0) {
			for (int i = 0; i < interceptors.length; i++) {
				HandlerInterceptor interceptor = interceptors[i];
				if (!interceptor.preHandle(req)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void finishHandle(ServletWebRequest req, Object result, Exception e) {
		HandlerMethod hm = getHandlerMethod(req);
		if (hm.getSkip()) {
			logger.debug("Skip all Interceptors finishHandle.");
			return;
		}
		if (this.interceptors.length > 0) {
			for (int i = interceptors.length - 1; i >= 0; i--) {
				HandlerInterceptor interceptor = interceptors[i];
				interceptor.finishHandle(req, result, e);
			}
		}
	}

	public Object handleRequest(ServletWebRequest req) throws Exception {
		Controller ctrl = null;
		try {
			HandlerMethod hm = getHandlerMethod(req);
			ctrl = (Controller) this.context.getSingleton(hm.getController());
			ctrl.init(req);
			Object[] args = buildArgs(hm.getParameters(), ctrl, req);
			return hm.invoke(ctrl, args);
		} finally {
			if (ctrl != null) {
				ctrl.clear();
			}
		}
	}

	private HandlerMethod getHandlerMethod(ServletWebRequest req) {
		return this.handlerMethods.get(req.getServletPath());
	}

	private static Object[] buildArgs(Parameter[] parameters, Controller ctrl, ServletWebRequest req) {
		Object[] args = new Object[parameters.length];
		if (parameters.length > 0) {
			Param p;
			for (int i = 0; i < parameters.length; i++) {
				p = parameters[i].getAnnotation(Param.class);
				if (p != null) {
					args[i] = ctrl.getParameter(parameters[i].getType(), p.value());
				} else {
					if (parameters[i].getType().isAssignableFrom(HttpServletRequest.class)) {
						args[i] = req.getRequest();
					} else if (parameters[i].getType().isAssignableFrom(HttpServletResponse.class)) {
						args[i] = req.getResponse();
					} else if (parameters[i].getType().isAssignableFrom(HttpSession.class)) {
						args[i] = req.getSession();
					}
				}
			}
		}
		return args;
	}

	private void detectHandlers(Class<?>[] classes) {
		for (Class<?> clazz : classes) {
			if (Controller.class.isAssignableFrom(clazz)) {
				registerHandler(clazz);
			}
		}
	}

	private void detectInterceptor(Class<?>[] classes) {
		List<HandlerInterceptor> temp = new ArrayList<>();
		for (Class<?> clazz : classes) {
			if (HandlerInterceptor.class.isAssignableFrom(clazz)) {
				registerInterceptor(temp, clazz);
			}
		}
		OrderWrapper.sort(temp);
		this.interceptors = new HandlerInterceptor[temp.size()];
		temp.toArray(this.interceptors);
	}

	private void registerHandler(Class<?> temp) {
		Method[] methods = temp.getDeclaredMethods();
		Request r1 = temp.getAnnotation(Request.class);
		String path;
		HandlerMethod hm;
		for (Method method : methods) {
			Request r2 = method.getAnnotation(Request.class);
			if (r2 != null) {
				path = BeanUtils.genericPath(r1, r2);
				hm = new HandlerMethod(method);
				hm.setSkip(temp.getAnnotation(Skip.class) != null || method.getAnnotation(Skip.class) != null);
				this.handlerMethods.put(path, hm);
			}
		}
	}

	private void registerInterceptor(List<HandlerInterceptor> temp, Class<?> clazz) {
		temp.add((HandlerInterceptor) this.context.getSingleton(clazz.getName()));
	}
}
