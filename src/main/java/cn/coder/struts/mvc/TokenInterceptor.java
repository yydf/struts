package cn.coder.struts.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * token拦截器实现的基础类
 * 
 * @author YYDF
 *
 */
public abstract class TokenInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res) {
		boolean has = hasPermission(req);
		if (!has) {
			noAuthorizaiton(req);
			return false;
		}
		return true;
	}

	protected abstract void noAuthorizaiton(HttpServletRequest req);

	protected abstract boolean hasPermission(HttpServletRequest req);

	@Override
	public void finish(HttpServletRequest req, HttpServletResponse res, Object result, Exception e) {
		// TODO Auto-generated method stub

	}

}
