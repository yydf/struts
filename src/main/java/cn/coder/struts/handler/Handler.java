package cn.coder.struts.handler;

import cn.coder.struts.support.ServletWebRequest;

public interface Handler {

	boolean lookup(ServletWebRequest req);

	boolean preHandle(ServletWebRequest req);

	void finishHandle(ServletWebRequest req, Object result, Exception dispatchException);

}
