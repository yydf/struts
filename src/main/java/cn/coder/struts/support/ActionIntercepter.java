package cn.coder.struts.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
