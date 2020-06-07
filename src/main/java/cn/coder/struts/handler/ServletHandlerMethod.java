package cn.coder.struts.handler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cn.coder.struts.annotation.Param;
import cn.coder.struts.mvc.Controller;

public final class ServletHandlerMethod {

	private final HandlerMethod hm;

	public ServletHandlerMethod(HandlerMethod handler) {
		this.hm = handler;
	}

	public Object handle(HttpServletRequest req, HttpServletResponse res) throws Exception {
		matchRequest(req);
		return handleResult(req, res);
	}

	private void matchRequest(HttpServletRequest req) {
		List<String> matched = this.hm.getMathed();
		if (matched == null || matched.isEmpty())
			return;
		Matcher matcher = (Matcher) req.getAttribute("struts.servlet.request.method.matcher");
		int num = 1;
		for (String para : matched) {
			req.setAttribute(para, matcher.group(num));
			num++;
		}
		matcher = null;
		req.removeAttribute("struts.servlet.request.method.matcher");
	}

	private Object handleResult(HttpServletRequest req, HttpServletResponse res) throws Exception {
		Controller bean = (Controller) this.hm.getBean();
		Method m = this.hm.getMethod();
		Object[] args = buildArgs(hm.getParameters(), bean, req, res);
		return m.invoke(bean, args);
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
