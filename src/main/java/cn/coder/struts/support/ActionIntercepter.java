package cn.coder.struts.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 全局拦截器，会在所有的Action执行之前运行
 * 
 * @author YYDF
 *
 */
public abstract class ActionIntercepter {
	/**
	 * 请求拦截器
	 * 
	 * @param req
	 *            请求
	 * @param res
	 *            响应
	 * @return 是否通过
	 */
	public abstract boolean intercept(HttpServletRequest req, HttpServletResponse res);
}
