package cn.coder.struts.handler;

import cn.coder.struts.core.ApplicationContext;
import cn.coder.struts.support.ServletWebRequest;

public class SimpleHandlerAdapter implements HandlerAdapter {

	public SimpleHandlerAdapter(ApplicationContext applicationContext) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean supports(Handler handler) {
		return (handler instanceof AbstractHandler);
	}

	@Override
	public Object handle(ServletWebRequest req, Handler handler) throws Exception {
		return ((AbstractHandler) handler).handleRequest(req);
	}

}
