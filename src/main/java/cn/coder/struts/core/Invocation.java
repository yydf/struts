package cn.coder.struts.core;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.aop.Aop;
import cn.coder.struts.support.Interceptor;
import cn.coder.struts.view.Action;

public final class Invocation {

	private int num = -1;
	private int size;
	private Action action;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private List<Class<?>> interceptors;

	public Invocation(HttpServletRequest req, HttpServletResponse res, Action action) {
		this.action = action;
		this.interceptors = action.getInterceptors();
		this.size = this.interceptors.size();
		next();
	}

	public void next() {
		this.num++;
		if (this.num < size) {
			((Interceptor) Aop.create(interceptors.get(this.num))).intercept(this);
		}
	}

	public boolean complete() {
		return this.num == size;
	}

	public Action getAction() {
		return this.action;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

}
