package cn.coder.struts.handler;

import cn.coder.struts.core.ApplicationContext;
import cn.coder.struts.support.ServletWebRequest;

public class SimpleRequestAdapter implements HandlerAdapter {

	public SimpleRequestAdapter(ApplicationContext applicationContext) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean supports(Handler handler) {
		return (handler instanceof SimpleRequestHandler);
	}

	@Override
	public Object handle(ServletWebRequest req, Handler handler) throws Exception {
		return ((SimpleRequestHandler) handler).handleRequest(req);
	}

}
