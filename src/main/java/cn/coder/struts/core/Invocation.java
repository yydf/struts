package cn.coder.struts.core;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.aop.Aop;
import cn.coder.struts.support.Interceptor;

public final class Invocation {

	private int size;
	private int num = -1;
	private Action action;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private ArrayList<Class<?>> interceptors;

	public Invocation(HttpServletRequest req, HttpServletResponse res, Action action) {
		this.request = req;
		this.response = res;
		this.action = action;
		this.interceptors = action.getInterceptors();
		this.size = (this.interceptors != null ? this.interceptors.size() : 0);
		next();
	}

	public void next() {
		this.num++;
		if (this.num < size) {
			((Interceptor) Aop.create(interceptors.get(this.num))).intercept(this);
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
		return this.num == size;
	}

}
