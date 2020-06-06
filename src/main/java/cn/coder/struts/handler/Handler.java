package cn.coder.struts.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Handler {

	boolean lookup(HttpServletRequest req);

	boolean preHandle(HttpServletRequest request, HttpServletResponse response);

	void finish(HttpServletRequest req, HttpServletResponse res, Object result, Exception error);

}
