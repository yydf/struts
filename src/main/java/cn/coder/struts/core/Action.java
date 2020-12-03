package cn.coder.struts.core;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import cn.coder.struts.annotation.Request;
import cn.coder.struts.annotation.Request.HttpMethod;

public final class Action {

	private final Method method;
	private final Parameter[] parameters;
	private final Class<?> controller;
	private final HttpMethod httpMethod;

	public Action(Method method) {
		this.method = method;
		this.httpMethod = method.getAnnotation(Request.class).method();
		this.parameters = method.getParameters();
		this.controller = method.getDeclaringClass();
	}

	public Class<?> getController() {
		return this.controller;
	}

	public Method getMethod() {
		return this.method;
	}

	public Parameter[] getParameters() {
		return this.parameters;
	}

	public String getHttpMethod() {
		return this.httpMethod.name();
	}

}
