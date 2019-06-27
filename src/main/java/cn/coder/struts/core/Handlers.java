package cn.coder.struts.core;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.aop.Aop;
import cn.coder.struts.support.Handler;

public final class Handlers {

	private int size;
	private int num = -1;
	private List<Class<?>> handlers;

	public Handlers(List<Class<?>> handlers, HttpServletRequest req, HttpServletResponse res) {
		this.size = handlers.size();
		this.handlers = handlers;
		next(req, res);
	}

	public void next(HttpServletRequest req, HttpServletResponse res) {
		this.num++;
		if (this.num < size) {
			((Handler) Aop.create(handlers.get(this.num))).handle(req, res, this);
		}
	}

	public boolean complete() {
		return this.num == size;
	}
}
