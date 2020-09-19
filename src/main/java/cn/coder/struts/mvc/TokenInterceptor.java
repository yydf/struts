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
	
	//判断是否有权限
	protected abstract boolean hasPermission(HttpServletRequest request);
	
	//没有权限的处理
	protected abstract void noAuthorizaiton(HttpServletRequest request, HttpServletResponse response);
	
	@Override
	public boolean before(Object[] args) throws Exception {
		HttpServletRequest request = (HttpServletRequest) args[0];
		if (!hasPermission(request)) {
			noAuthorizaiton(request, (HttpServletResponse)args[1]);
			return false;
		}
		return true;
	}

	@Override
	public void after(Object result) throws Exception {
	}

	@Override
	public void exceptionCaught(Exception exception) throws Throwable {
	}

	
}
