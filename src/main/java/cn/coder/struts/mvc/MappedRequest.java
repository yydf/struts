package cn.coder.struts.mvc;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public class MappedRequest {

	private final Object bean;
	private final List<String> mathed;
	private final Method method;
	private final boolean skip;
	private final Parameter[] parameters;

	public MappedRequest(Object bean, Method method, boolean skip) {
		this(bean, method, skip, null);
	}

	public MappedRequest(Object bean, Method method, boolean skip, List<String> paras) {
		this.bean = bean;
		this.method = method;
		this.parameters = method.getParameters();
		this.skip = skip;
		this.mathed = paras;
	}

	public List<String> getMathed() {
		return this.mathed;
	}

	public Method getMethod() {
		return this.method;
	}

	public Parameter[] getParameters() {
		return this.parameters;
	}

	public Object getBean() {
		return this.bean;
	}

	public boolean isSkip() {
		return this.skip;
	}

}
