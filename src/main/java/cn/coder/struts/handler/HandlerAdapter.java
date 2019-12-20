package cn.coder.struts.handler;

import cn.coder.struts.support.ServletWebRequest;

public interface HandlerAdapter {

	boolean supports(Handler handler);

	Object handle(ServletWebRequest req, Handler handler) throws Exception;

}
