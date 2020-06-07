package cn.coder.struts.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Interceptor {

	boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler);

	void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, Object result);

	void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Throwable error);

}
