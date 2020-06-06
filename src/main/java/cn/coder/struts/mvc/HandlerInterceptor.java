package cn.coder.struts.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerInterceptor {

	boolean preHandle(HttpServletRequest req, HttpServletResponse res);

	void finish(HttpServletRequest req, HttpServletResponse res, Object result, Exception e);

}
