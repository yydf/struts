package cn.coder.struts.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import cn.coder.struts.StrutsApplicationContext;
import cn.coder.struts.annotation.Request;
import cn.coder.struts.util.BeanUtils;

public final class MatchableRequestMethodHandler extends AbstractRequestMethodHandler {

	public MatchableRequestMethodHandler(StrutsApplicationContext context) {
		super(context);
	}

	@Override
	protected Object lookupHandler(HttpServletRequest req) {
		String servletPath = req.getServletPath();
		Set<String> patterns = getHandlerMethods().keySet();
		for (String pattern : patterns) {
			Matcher match = Pattern.compile(pattern).matcher(servletPath);
			if (match.find()) {
				req.setAttribute("struts.servlet.request.method.matcher", match);
				return getHandlerMethods().get(pattern);
			}
		}
		return null;
	}

	@Override
	protected void registerHandler(Class<?> clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		Request r1 = clazz.getAnnotation(Request.class);
		String path;
		for (Method method : methods) {
			path = BeanUtils.genericPath(r1, method.getAnnotation(Request.class));
			if (matchablePath(path)) {
				createHandlerMethod(clazz, method, path);
			}
		}
	}

	private void createHandlerMethod(Class<?> clazz, Method method, String path) {
		List<String> paras = new ArrayList<>();
		String para;
		String pattern = path;
		while ((para = getParameter(pattern)) != null) {
			paras.add(para.replace("{", "").replace("}", ""));
			pattern = pattern.replace(para, "(.*)");
		}
		boolean skip = getSkip(clazz, method);
		Object bean = getApplicationContext().getBean(clazz.getName());
		getHandlerMethods().put(pattern, new HandlerMethod(bean, method, skip, paras));
	}

	private static String getParameter(String action) {
		int begin = action.indexOf("{");
		if (begin == -1)
			return null;
		int end = action.indexOf("}", begin) + 1;
		return action.substring(begin, end);
	}
}
