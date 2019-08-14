package cn.coder.struts.core;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;

import cn.coder.struts.annotation.Request;
import cn.coder.struts.annotation.Request.HttpMethod;
import cn.coder.struts.wrapper.OrderWrapper;

public final class Action {

	private final Method method;
	private final Parameter[] parameters;
	private final Class<?> controller;
	private final HttpMethod[] httpMethods;
	private ArrayList<Class<?>> interceptors;

	public Action(Method method) {
		this.method = method;
		this.httpMethods = method.getAnnotation(Request.class).method();
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

	public void setInterceptors(Class<?>[] classes) {
		if (classes.length > 0) {
			// 去重
			this.interceptors = new ArrayList<>();
			for (Class<?> clazz : classes) {
				if (!this.interceptors.contains(clazz))
					this.interceptors.add(clazz);
			}
			// 排序
			OrderWrapper.sort(this.interceptors);
		}
	}

	public ArrayList<Class<?>> getInterceptors() {
		return this.interceptors;
	}

	public boolean sameMethod(String method) {
		for (HttpMethod httpMethod : httpMethods) {
			if(httpMethod.name().equals(method))
				return true;
		}
		return false;
	}

}
