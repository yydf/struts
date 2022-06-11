package cn.coder.struts.core;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
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

import cn.coder.struts.annotation.Param;
import cn.coder.struts.annotation.Request;
import cn.coder.struts.annotation.Skip;
import cn.coder.struts.aop.Aop;
import cn.coder.struts.support.ActionSupport;
import cn.coder.struts.util.BeanUtils;
import cn.coder.struts.util.ObjectUtils;
import cn.coder.struts.wrapper.MultipartRequestWrapper;
import cn.coder.struts.wrapper.OrderWrapper;
import cn.coder.struts.wrapper.ResponseWrapper;

public final class ActionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ActionHandler.class);

	private static final ArrayList<Class<?>> EMPTY_INTERCEPTORS = new ArrayList<>();
	private static final String MULTIPART_ATTRIBUTE = "struts.servlet.multipart.wrapper";

	private ArrayList<Class<?>> interceptors;
	private HashMap<String, Action> mappings = new HashMap<>();
	private ResponseWrapper responseWrapper = new ResponseWrapper();
	private boolean multipartContent = false;
	private String basePath;

	public ActionHandler(List<Class<?>> controllers, ArrayList<Class<?>> interceptors) {
		OrderWrapper.sort(interceptors);
		this.interceptors = interceptors;
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

	@SuppressWarnings("unchecked")
	public ArrayList<Class<?>> buildInterceptors(Action action) {
		Skip skip = action.getMethod().getAnnotation(Skip.class);
		// 当前函数不跳过拦截器
		if (skip == null) {
			skip = action.getController().getAnnotation(Skip.class);
			// 当前Controller也不跳过拦截器，返回全部拦截器
			if (skip == null) {
				return this.interceptors;
			} else {
				// 跳过所有拦截器
				if (skip.value().length == 0) {
					return EMPTY_INTERCEPTORS;
				} else {
					// 跳过skip中设置的拦截器
					ArrayList<Class<?>> temp = new ArrayList<>();
					temp.addAll(this.interceptors);
					return (ArrayList<Class<?>>) BeanUtils.removeSameItem(temp, skip.value());
				}
			}
		}
		// 跳过所有拦截器
		if (skip.value().length == 0) {
			return EMPTY_INTERCEPTORS;
		}
		Skip skip2 = action.getController().getAnnotation(Skip.class);
		// 当前Controller也不跳过拦截器，返回全部拦截器
		if (skip2 == null) {
			// 跳过skip中设置的拦截器
			ArrayList<Class<?>> temp = new ArrayList<>();
			temp.addAll(this.interceptors);
			return (ArrayList<Class<?>>) BeanUtils.removeSameItem(temp, skip.value());
		} else {
			if (skip2.value().length == 0) {
				return EMPTY_INTERCEPTORS;
			}
			// 跳过skip中设置的拦截器
			Object[] arr = ObjectUtils.merge(skip.value(), skip2.value());
			ArrayList<Class<?>> temp = new ArrayList<>();
			temp.addAll(this.interceptors);
			return (ArrayList<Class<?>>) BeanUtils.removeSameItem(temp, arr);
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
		Invocation inv = new Invocation(req, res, action, buildInterceptors(action));
		if (!inv.complete()) {
			if (logger.isDebugEnabled())
				logger.debug("Action stoped by interceptor '{}'", inv.current().getName());
			return;
		}
		ActionSupport support = null;
		try {
			support = (ActionSupport) Aop.create(action.getController());

			checkMultipart(req, support);

			req.setAttribute("basePath", getBasePath(req));

			Object[] args = buildArgs(action, support, req, res);
			Object result = action.getMethod().invoke(support, args);
			if (result != null) {
				responseWrapper.doResponse(result, req, res);
			}
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error("Handle action faild", e);
		} finally {
			clearMultipart(req);
			if (support != null) {
				support.clear();
			}
		}
	}

	private void checkMultipart(HttpServletRequest request, ActionSupport support) {
		this.multipartContent = MultipartRequestWrapper.isMultipartContent(request);
		if (this.multipartContent) {
			request.setAttribute(MULTIPART_ATTRIBUTE, new MultipartRequestWrapper(request, support));
		}
	}

	private void clearMultipart(HttpServletRequest request) {
		if (this.multipartContent) {
			((MultipartRequestWrapper) request.getAttribute(MULTIPART_ATTRIBUTE)).clear();
			request.removeAttribute(MULTIPART_ATTRIBUTE);
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

	private String getBasePath(HttpServletRequest request) {
		if (this.basePath == null) {
			String path = request.getContextPath();
			if ("/".equals(path))
				this.basePath = "";
			else
				this.basePath = request.getContextPath() + "/";
		}
		return this.basePath;
	}

	public synchronized void clear() {
		this.interceptors.clear();
		mappings.clear();
	}

}
