package cn.coder.struts.wrapper;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.annotation.Before;
import cn.coder.struts.annotation.Request;
import cn.coder.struts.annotation.Skip;
import cn.coder.struts.aop.Aop;
import cn.coder.struts.core.Invocation;
import cn.coder.struts.support.ActionSupport;
import cn.coder.struts.util.ClassUtils;
import cn.coder.struts.view.Action;

public final class ActionWrapper {
	private static final Logger logger = LoggerFactory.getLogger(ActionWrapper.class);

	private HashMap<String, Action> mappings = new HashMap<>();
	private ResponseWrapper responseWrapper;

	public ActionWrapper(List<Class<?>> classes, List<Class<?>> interceptors) {
		if (classes != null) {
			buildMapping(classes);
			buildInterceptors(interceptors);
		}
		this.responseWrapper = new ResponseWrapper();
	}

	private void buildMapping(List<Class<?>> classes) {
		Request req;
		Request classReq;
		for (Class<?> clazz : classes) {
			classReq = clazz.getAnnotation(Request.class);
			Method[] methods = clazz.getDeclaredMethods();
			for (Method method : methods) {
				req = method.getAnnotation(Request.class);
				if (req != null) {
					mappings.put(ClassUtils.getUrlMapping(classReq, req.value()), new Action(method));
				} else {
					mappings.put(ClassUtils.getUrlMapping(classReq, method.getName()), new Action(method));
				}
			}
		}
	}

	private void buildInterceptors(List<Class<?>> interceptors) {
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

	public Action getAction(HttpServletRequest req) {
		return mappings.get(req.getServletPath());
	}

	public void execute(Action action, HttpServletRequest req, HttpServletResponse res) throws ServletException {
		ActionSupport support = null;
		try {
			Invocation invocation = new Invocation(req, res, action);
			if (!invocation.complete()) {
				if (logger.isDebugEnabled())
					logger.debug("Interceptor stoped");
				return;
			}
			support = (ActionSupport) Aop.create(action.getController());
			support.init(req, res);
			Object result = action.getMethod().invoke(support);
			if (result != null) {
				responseWrapper.doResponse(result, req, res);
			}
		} catch (Exception e) {
			throw new ServletException("运行Action失败", e);
		} finally {
			if (support != null) {
				support.clear();
			}
		}
	}

	public synchronized void clear() {
		this.mappings.clear();
		this.responseWrapper = null;
	}

}
