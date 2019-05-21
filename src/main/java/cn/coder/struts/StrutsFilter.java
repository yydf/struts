package cn.coder.struts;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.support.ActionIntercepter;
import cn.coder.struts.support.WebInitializer;
import cn.coder.struts.util.ClassUtils;
import cn.coder.struts.wrapper.ActionWrapper;
import cn.coder.struts.wrapper.ResponseWrapper;

/**
 * 核心控制类，包括初始化Action和注入@Resource对象
 * 
 * @author YYDF
 *
 */
public class StrutsFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(StrutsFilter.class);
	private ResponseWrapper wrapper;
	private ActionWrapper actionWrapper;
	private ArrayList<WebInitializer> initArray;
	private ArrayList<ActionIntercepter> filters;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		initWebInitializer(filterConfig.getServletContext());
		initFilter(filterConfig.getServletContext());
		initAction(filterConfig.getServletContext());
		wrapper = new ResponseWrapper();
	}

	@SuppressWarnings("unchecked")
	private void initFilter(ServletContext sc) {
		filters = (ArrayList<ActionIntercepter>) sc.getAttribute("Filters");
		sc.removeAttribute("Filters");
	}

	@SuppressWarnings("unchecked")
	private void initWebInitializer(ServletContext sc) {
		Set<Class<?>> InitializerClasses = (Set<Class<?>>) sc.getAttribute("InitializerClasses");
		sc.removeAttribute("InitializerClasses");
		if (InitializerClasses != null && InitializerClasses.size() > 0) {
			initArray = new ArrayList<>();
			WebInitializer initObj;
			for (Class<?> clazz : InitializerClasses) {
				try {
					initObj = (WebInitializer) clazz.newInstance();
					initObj.onStartup(sc);
					initArray.add(initObj);
				} catch (Exception e) {
					logger.error("WebInitializer start faild", e);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void initAction(ServletContext sc) throws ServletException {
		actionWrapper = (ActionWrapper) sc.getAttribute("ActionWrapper");
		sc.removeAttribute("ActionWrapper");
		HashMap<Class<?>, Object> classes = (HashMap<Class<?>, Object>) sc.getAttribute("Classes");
		sc.removeAttribute("Classes");
		FilterRegistration filter = sc.getFilterRegistration("StrutsFilter");
		Set<String> mappedUrls = actionWrapper.getMappedUrls();
		if (!mappedUrls.isEmpty()) {
			EnumSet<DispatcherType> dispatcherTypes = EnumSet.allOf(DispatcherType.class);
			dispatcherTypes.add(DispatcherType.REQUEST);
			dispatcherTypes.add(DispatcherType.FORWARD);
			for (String action : mappedUrls) {
				filter.addMappingForUrlPatterns(dispatcherTypes, true, action);
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
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		long start = System.currentTimeMillis();
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		HttpServletRequest req = (HttpServletRequest) request;
		if (logger.isDebugEnabled())
			logger.debug("Request [{}]{}", req.getMethod(), req.getServletPath());
		if (req.getMethod().equals("OPTIONS")) {
			chain.doFilter(request, response);
			return;
		}
		HttpServletResponse res = (HttpServletResponse) response;
		if (!checkFilter(req, res)) {
			if (logger.isDebugEnabled())
				logger.debug("Action stoped by filter");
			return;
		}
		Method method = actionWrapper.getActionMethod(req.getServletPath());
		if (method != null) {
			if (!ClassUtils.allowHttpMethod(method, req.getMethod())) {
				res.sendError(405, "Request method '" + req.getMethod() + "' not supported");
				if (logger.isDebugEnabled())
					logger.debug("{} method not allowed", req.getMethod());
				return;
			}
			Object result = actionWrapper.execute(method, req, res);
			if (result != null) {
				wrapper.doResponse(result, req, res);
			}
		} else {
			chain.doFilter(request, response);
		}
		if (logger.isDebugEnabled())
			logger.debug("Request finished with {}ms", (System.currentTimeMillis() - start));
	}

	private boolean checkFilter(HttpServletRequest req, HttpServletResponse res) {
		if (filters == null || filters.isEmpty())
			return true;
		for (ActionIntercepter intercepter : filters) {
			if (!intercepter.intercept(req, res))
				return false;
		}
		return true;
	}

	@Override
	public void destroy() {
		if (actionWrapper != null) {
			actionWrapper.clear();
			actionWrapper = null;
		}
		if (initArray != null) {
			for (WebInitializer webInitializer : initArray) {
				try {
					webInitializer.destroy();
				} catch (Exception e) {
					logger.error("WebInitializer destroy faild", e);
				}
			}
		}
		if (filters != null) {
			filters.clear();
			filters = null;
		}
		wrapper = null;
	}

}
