package cn.coder.struts.support;

public interface HandlerInterceptor {

	boolean preHandle(ServletWebRequest req);

	void finishHandle(ServletWebRequest req, Object result, Exception e);

}
