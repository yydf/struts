package cn.coder.struts.view;

import java.lang.reflect.Method;
import java.util.ArrayList;

import cn.coder.struts.wrapper.OrderWrapper;

public final class Action {

	private Method method;
	private Class<?> controller;
	private ArrayList<Class<?>> interceptors;

	public Action(Method method) {
		this.method = method;
		this.controller = method.getDeclaringClass();
	}

	public Method getMethod() {
		return this.method;
	}

	public Class<?> getController() {
		return this.controller;
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

}
