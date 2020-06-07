package cn.coder.struts.handler;

import javax.servlet.http.HttpServletRequest;

public interface Handler {

	HandlerChain getHandlerChain(HttpServletRequest req);

}
