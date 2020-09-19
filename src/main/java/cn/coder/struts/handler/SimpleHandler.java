package cn.coder.struts.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import cn.coder.struts.StrutsApplicationContext;
import cn.coder.struts.annotation.Request;
import cn.coder.struts.annotation.Skip;
import cn.coder.struts.mvc.Controller;
import cn.coder.struts.mvc.Interceptor;
import cn.coder.struts.mvc.MappedRequest;
import cn.coder.struts.mvc.RequestInterceptor;
import cn.coder.struts.util.BeanUtils;

public class SimpleHandler implements Handler {
	private final Map<String, MappedRequest> simpleRequests = new LinkedHashMap<>();
	private final Map<String, MappedRequest> matchedRequests = new LinkedHashMap<>();
	private final StrutsApplicationContext context;

	public SimpleHandler(StrutsApplicationContext sac) {
		this.context = sac;
		detectController();
	}

	private void detectController() {
		Class<?>[] beanNames = this.context.getBeanNamesByType(Controller.class);
		for (Class<?> clazz : beanNames) {
			detectRequestMapping(clazz);
		}
	}

	private void detectRequestMapping(Class<?> clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		Request r1 = clazz.getAnnotation(Request.class);
		String path;
		for (Method method : methods) {
			path = BeanUtils.genericPath(r1, method.getAnnotation(Request.class));
			if (path != null) {
				boolean skip = getSkip(clazz, method);
				Object bean = this.context.getBean(clazz.getName());
				if (isMatchablePath(path)) {
					addMatchedRequest(path, bean, method, skip);
				} else {
					addSimpleRequest(path, bean, method, skip);
				}
			}
		}
	}

	private void addSimpleRequest(String path, Object bean, Method method, boolean skip) {
		this.simpleRequests.put(path, new MappedRequest(bean, method, skip));
	}

	private void addMatchedRequest(String path, Object bean, Method method, boolean skip) {
		List<String> paras = new ArrayList<>();
		String para;
		String pattern = path;
		while ((para = getParameter(pattern)) != null) {
			paras.add(para.replace("{", "").replace("}", ""));
			pattern = pattern.replace(para, "(.*)");
		}
		this.matchedRequests.put(pattern, new MappedRequest(bean, method, skip, paras));
	}

	private static boolean isMatchablePath(String path) {
		return path.contains("{") && path.contains("}");
	}

	private static String getParameter(String action) {
		int begin = action.indexOf("{");
		if (begin == -1)
			return null;
		int end = action.indexOf("}", begin) + 1;
		return action.substring(begin, end);
	}

	private static boolean getSkip(Class<?> clazz, Method method) {
		return clazz.getAnnotation(Skip.class) != null || method.getAnnotation(Skip.class) != null;
	}

	@Override
	public SimpleExecutor getExecutor(HttpServletRequest req) {
		Object handler = lookupHandler(req);
		if (handler == null)
			return null;
		SimpleExecutor executor = new SimpleExecutor(handler);

		String lookupPath = req.getServletPath();
		List<Interceptor> interceptors = this.context.getInterceptors();
		for (Interceptor temp : interceptors) {
			if (temp instanceof RequestInterceptor) {
				if (((RequestInterceptor) temp).matches(lookupPath)) {
					executor.addInterceptor(temp);
				}
			} else {
				executor.addInterceptor(temp);
			}
		}
		return executor;
	}

	private Object lookupHandler(HttpServletRequest req) {
		MappedRequest handler = this.simpleRequests.get(req.getServletPath());
		if (handler == null) {
			handler = matchedHandler(req);
		}
		return handler;
	}

	private MappedRequest matchedHandler(HttpServletRequest req) {
		String servletPath = req.getServletPath();
		Set<String> patterns = this.matchedRequests.keySet();
		for (String pattern : patterns) {
			Matcher match = Pattern.compile(pattern).matcher(servletPath);
			if (match.find()) {
				req.setAttribute("struts.servlet.request.method.matcher", match);
				return this.matchedRequests.get(pattern);
			}
		}
		return null;
	}

}
