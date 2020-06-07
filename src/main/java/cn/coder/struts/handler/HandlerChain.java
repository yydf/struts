package cn.coder.struts.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.mvc.Interceptor;

public final class HandlerChain {

	private final Object handler;
	private Interceptor[] interceptors;
	private int interceptorIndex = -1;

	public HandlerChain(Object handler) {
		this.handler = handler;
		this.interceptors = new Interceptor[0];
	}

	public Object getHandler() {
		return this.handler;
	}

	public boolean doPreHandle(HttpServletRequest request, HttpServletResponse response) {
		if (this.handler instanceof HandlerMethod) {
			// 如果设置了跳过验证
			if (((HandlerMethod) this.handler).getSkiped())
				return true;
		}
		if (this.interceptors.length > 0) {
			for (int i = 0; i < this.interceptors.length; i++) {
				if (!this.interceptors[i].preHandle(request, response, this.handler)) {
					triggerAfterCompletion(request, response, null);
					return false;
				}
				this.interceptorIndex = i;
			}
		}
		return true;
	}

	public void doPostHandle(HttpServletRequest request, HttpServletResponse response, Object result) {
		if (this.interceptors.length > 0) {
			for (int i = 0; i < this.interceptors.length; i++) {
				this.interceptors[i].postHandle(request, response, this.handler, result);
			}
		}
	}

	private void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, Throwable error) {
		if (this.interceptors.length > 0) {
			for (int i = this.interceptorIndex; i >= 0; i--) {
				try {
					this.interceptors[i].afterCompletion(request, response, this.handler, error);
				} catch (Throwable e) {
					// Do Nothing
				}
			}
		}
	}

	public void addInterceptor(Interceptor interceptor) {
		Interceptor[] temp = new Interceptor[this.interceptors.length + 1];
		System.arraycopy(this.interceptors, 0, temp, 0, this.interceptors.length);
		temp[this.interceptors.length] = interceptor;
		this.interceptors = temp;
	}

}
