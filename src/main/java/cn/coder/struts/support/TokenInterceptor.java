package cn.coder.struts.support;

/**
 * token拦截器实现的基础类
 * 
 * @author YYDF
 *
 */
public abstract class TokenInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(ServletWebRequest req) {
		boolean has = hasPermission(req);
		if (!has) {
			noAuthorizaiton(req);
			return false;
		}
		return true;
	}

	protected abstract void noAuthorizaiton(ServletWebRequest req);

	protected abstract boolean hasPermission(ServletWebRequest req);

	@Override
	public void finishHandle(ServletWebRequest req, Object result, Exception e) {
		// TODO Auto-generated method stub

	}

}
