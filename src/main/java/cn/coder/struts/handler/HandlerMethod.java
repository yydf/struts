package cn.coder.struts.handler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;

import cn.coder.struts.mvc.Controller;

public final class HandlerMethod {

	private final Method method;
	private final Class<?> controller;
	private final Parameter[] parameters;
	private boolean skip;
	private List<String> matched;
	private HashMap<String, String> paraValues;

	public HandlerMethod(Method method) {
		this.method = method;
		this.parameters = method.getParameters();
		this.controller = method.getDeclaringClass();
		this.paraValues = new HashMap<>();
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

	public void matchValues(Matcher match) {
		paraValues.clear();
		int num = 1;
		for (String para : this.matched) {
			paraValues.put(para, match.group(num));
			num++;
		}
	}

	public void setMatch(List<String> paras) {
		this.matched = paras;
	}

	public boolean hasMatchedValues() {
		return this.paraValues.size() > 0;
	}

	public void fillRequest(HttpServletRequest req) {
		for (String para : this.matched) {
			req.setAttribute(para, this.paraValues.get(para));
		}
	}

}
