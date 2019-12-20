package cn.coder.struts.handler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import cn.coder.struts.support.Controller;

public final class HandlerMethod {

	private final Method method;
	private final Class<?> controller;
	private final Parameter[] parameters;
	private boolean skip;

	public HandlerMethod(Method method) {
		this.method = method;
		this.parameters = method.getParameters();
		this.controller = method.getDeclaringClass();
	}

	public String getController() {
		return this.controller.getName();
	}

	public Object invoke(Controller ctrl, Object[] args) throws Exception {
		return this.method.invoke(ctrl, args);
	}

	public Parameter[] getParameters() {
		return this.parameters;
	}

	public void setSkip(boolean s) {
		this.skip = s;
	}

	public boolean getSkip() {
		return this.skip;
	}

}
