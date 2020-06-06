package cn.coder.struts.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.StrutsApplicationContext;

public final class SimpleHandlerAdapter implements HandlerAdapter {

	public SimpleHandlerAdapter(StrutsApplicationContext context) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean supports(Handler handler) {
		return (handler instanceof AbstractHandler);
	}

	@Override
	public Object handle(HttpServletRequest req, HttpServletResponse res, Handler handler) throws Exception {
		return ((AbstractHandler) handler).handleRequest(req, res);
	}

}
