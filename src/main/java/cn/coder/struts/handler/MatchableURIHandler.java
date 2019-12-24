package cn.coder.struts.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.coder.struts.annotation.Request;
import cn.coder.struts.annotation.Skip;
import cn.coder.struts.core.ApplicationContext;
import cn.coder.struts.support.ServletWebRequest;
import cn.coder.struts.util.BeanUtils;

public class MatchableURIHandler extends AbstractHandler {

	public MatchableURIHandler(ApplicationContext context) {
		super(context);
	}

	@Override
	protected HandlerMethod getHandlerMethod(ServletWebRequest req) {
		if (handlerMethods.isEmpty())
			return null;
		HandlerMethod hm = null;
		String servletPath = req.getServletPath();
		Set<String> patterns = handlerMethods.keySet();
		for (String pattern : patterns) {
			Matcher match = Pattern.compile(pattern).matcher(servletPath);
			while (match.find()) {
				hm = handlerMethods.get(pattern);
				hm.matchValues(match);
				break;
			}
		}
		return hm;
	}

	@Override
	protected void registerHandler(Class<?> temp) {
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
				ss(hm, path);
			}
		}
	}

	private void ss(HandlerMethod hm, String path) {
		if (path.contains("{") && path.contains("}")) {
			List<String> paras = new ArrayList<>();
			String para;
			String pattern = path;
			while ((para = getParameter(pattern)) != null) {
				paras.add(para.replace("{", "").replace("}", ""));
				pattern = pattern.replace(para, "(.*)");
			}
			hm.setMatch(paras);
			this.handlerMethods.put(pattern, hm);
		}
	}

	private static String getParameter(String action) {
		int begin = action.indexOf("{");
		if (begin == -1)
			return null;
		int end = action.indexOf("}", begin) + 1;
		return action.substring(begin, end);
	}
}
