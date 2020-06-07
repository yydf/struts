package cn.coder.struts.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerAdapter {

	boolean supports(Object handler);

	Object handle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception;

}
