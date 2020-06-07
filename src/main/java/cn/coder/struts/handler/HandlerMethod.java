package cn.coder.struts.handler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public final class HandlerMethod {

	private final Object bean;
	private final Method method;
	private final boolean skiped;
	private final Parameter[] parameters;
	private final List<String> matched;

	public HandlerMethod(Object bean, Method m, boolean skip) {
		this(bean, m, skip, null);
	}

	public HandlerMethod(Object bean, Method m, boolean skip, List<String> paras) {
		this.bean = bean;
		this.method = m;
		this.skiped = skip;
		this.parameters = m.getParameters();
		this.matched = paras;
	}

	public boolean getSkiped() {
		return this.skiped;
	}

	public Object getBean() {
		return this.bean;
	}

	public Method getMethod() {
		return this.method;
	}

	public List<String> getMathed() {
		return this.matched;
	}

	public Parameter[] getParameters() {
		return this.parameters;
	}

}