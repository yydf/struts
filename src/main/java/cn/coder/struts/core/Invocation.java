package cn.coder.struts.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.aop.Aop;
import cn.coder.struts.support.Interceptor;

public final class Invocation {

	private int cur = 0;
	private final int size;
	private final Action action;
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final Class<?>[] interceptors;

	public Invocation(HttpServletRequest req, HttpServletResponse res, Action action) {
		this.request = req;
		this.response = res;
		this.action = action;
		this.interceptors = action.getInterceptors();
		this.size = (this.interceptors == null ? 0 : this.interceptors.length);
		next();
	}

	public void next() {
		if (this.cur < size) {
			((Interceptor) Aop.create(interceptors[this.cur++])).intercept(this);
		}
	}

	public Action getAction() {
		return this.action;
	}

	public HttpServletRequest getRequest() {
		return this.request;
	}

	public HttpServletResponse getResponse() {
		return this.response;
	}

	public boolean complete() {
		return this.cur == size;
	}

	public Class<?> current() {
		return interceptors[this.cur];
	}

}
