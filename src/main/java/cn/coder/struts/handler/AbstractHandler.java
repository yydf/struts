package cn.coder.struts.handler;

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

import cn.coder.struts.StrutsApplicationContext;
import cn.coder.struts.annotation.Param;
import cn.coder.struts.mvc.Controller;
import cn.coder.struts.mvc.HandlerInterceptor;
import cn.coder.struts.wrapper.OrderWrapper;

public abstract class AbstractHandler implements Handler {
	private static final Logger logger = LoggerFactory.getLogger(AbstractHandler.class);

	protected final StrutsApplicationContext context;
	protected final Map<String, HandlerMethod> handlerMethods;
	protected HandlerInterceptor[] interceptors;

	public AbstractHandler(StrutsApplicationContext context) {
		this.context = context;
		this.handlerMethods = new HashMap<>();
		detectHandlers(context.getBeanNamesByType(Controller.class));
		detectInterceptor(context.getBeanNamesByType(HandlerInterceptor.class));
	}

	@Override
	public boolean lookup(HttpServletRequest req) {
		return getHandlerMethod(req) != null;
	}

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res) {
		HandlerMethod hm = getHandlerMethod(req);
		if (hm.getSkip()) {
			logger.debug("Skip all Interceptors preHandle.");
			return true;
		}
		if (interceptors.length > 0) {
			for (int i = 0; i < interceptors.length; i++) {
				HandlerInterceptor interceptor = interceptors[i];
				if (!interceptor.preHandle(req, res)) {
					return false;
				}
			}
		}
		return true;
	}

	public Object handleRequest(HttpServletRequest req, HttpServletResponse res) throws Exception {
		Controller ctrl = null;
		try {
			HandlerMethod hm = getHandlerMethod(req);
			ctrl = (Controller) this.context.getBean(hm.getController());
			if (hm.hasMatchedValues()) {
				logger.debug("Set matched values to request attribute");
				hm.fillRequest(req);
			}
			Object[] args = buildArgs(hm.getParameters(), ctrl, req, res);
			return hm.invoke(ctrl, args);
		} finally {
//			if (ctrl != null) {
//				ctrl.clear();
//			}
		}
	}

	@Override
	public void finish(HttpServletRequest req, HttpServletResponse res, Object result, Exception e) {
		HandlerMethod hm = getHandlerMethod(req);
		if (hm.getSkip()) {
			logger.debug("Skip all Interceptors finishHandle.");
			return;
		}
		if (this.interceptors.length > 0) {
			for (int i = interceptors.length - 1; i >= 0; i--) {
				HandlerInterceptor interceptor = interceptors[i];
				interceptor.finish(req, res, result, e);
			}
		}
	}

	protected static Object[] buildArgs(Parameter[] parameters, Controller ctrl, HttpServletRequest req,
			HttpServletResponse res) {
		Object[] args = new Object[parameters.length];
		if (parameters.length > 0) {
			Param p;
			for (int i = 0; i < parameters.length; i++) {
				p = parameters[i].getAnnotation(Param.class);
				if (p != null) {
					args[i] = ctrl.getParameter(p.value(), parameters[i].getType());
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

	private void detectHandlers(List<Class<?>> classes) {
		for (Class<?> clazz : classes) {
			registerHandler(clazz);
		}
	}

	protected abstract HandlerMethod getHandlerMethod(HttpServletRequest req);

	protected abstract void registerHandler(Class<?> clazz);

	private void detectInterceptor(List<Class<?>> classes) {
		List<HandlerInterceptor> temp = new ArrayList<>();
		for (Class<?> clazz : classes) {
				registerInterceptor(temp, clazz);
		}
		OrderWrapper.sort(temp);
		this.interceptors = new HandlerInterceptor[temp.size()];
		temp.toArray(this.interceptors);
	}

	private void registerInterceptor(List<HandlerInterceptor> temp, Class<?> clazz) {
		temp.add((HandlerInterceptor) this.context.getBean(clazz.getName()));
	}
}
