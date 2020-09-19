package cn.coder.struts.handler;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cn.coder.struts.StrutsApplicationContext;
import cn.coder.struts.annotation.Param;
import cn.coder.struts.mvc.Controller;
import cn.coder.struts.mvc.MappedRequest;

public final class SimpleHandlerAdapter implements HandlerAdapter {

	private static final String STRUTS_SERVLET_MATCHER = "struts.servlet.request.method.matcher";

	public SimpleHandlerAdapter(StrutsApplicationContext sac) {
	}

	@Override
	public boolean supports(Object handler) {
		return (handler instanceof MappedRequest);
	}

	@Override
	public Object handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		matchRequest(request, (MappedRequest) handler);
		return handleResult(request, response, (MappedRequest) handler);
	}

	private void matchRequest(HttpServletRequest req, MappedRequest mr) {
		List<String> matched = mr.getMathed();
		if (matched == null || matched.isEmpty())
			return;
		Matcher matcher = (Matcher) req.getAttribute(STRUTS_SERVLET_MATCHER);
		int num = 1;
		for (String para : matched) {
			req.setAttribute(para, matcher.group(num));
			num++;
		}
		req.removeAttribute(STRUTS_SERVLET_MATCHER);
		matcher = null;
	}

	private Object handleResult(HttpServletRequest req, HttpServletResponse res, MappedRequest mr) throws Exception {
		Controller bean = (Controller) mr.getBean();
		Object[] args = buildArgs(mr.getParameters(), bean, req, res);
		return mr.getMethod().invoke(bean, args);
	}

	private static Object[] buildArgs(Parameter[] parameters, Controller ctrl, HttpServletRequest req,
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
}
