package cn.coder.struts.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * token拦截器实现的基础类
 * 
 * @author YYDF
 *
 */
public abstract class TokenInterceptor implements Interceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (!hasPermission(request)) {
			noAuthorizaiton(request, response);
			return false;
		}
		return true;
	}

	protected abstract boolean hasPermission(HttpServletRequest request);

	protected abstract void noAuthorizaiton(HttpServletRequest request, HttpServletResponse response);

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, Object result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Throwable error) {
		// TODO Auto-generated method stub

	}

}
