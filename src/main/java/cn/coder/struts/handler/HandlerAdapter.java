package cn.coder.struts.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerAdapter {

	boolean supports(Object handler);

	Object handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

}
