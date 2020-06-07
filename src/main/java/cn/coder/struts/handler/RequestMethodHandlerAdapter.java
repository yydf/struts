package cn.coder.struts.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class RequestMethodHandlerAdapter implements HandlerAdapter {

	@Override
	public boolean supports(Object handler) {
		return handler instanceof HandlerMethod;
	}

	@Override
	public Object handle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
		return new ServletHandlerMethod((HandlerMethod) handler).handle(req, res);
	}
}
