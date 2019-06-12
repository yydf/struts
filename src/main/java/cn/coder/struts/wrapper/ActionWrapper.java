package cn.coder.struts.wrapper;

import java.lang.reflect.Method;
import java.util.HashMap;

import cn.coder.struts.annotation.Request;
import cn.coder.struts.core.Action;
import cn.coder.struts.util.ClassUtils;

public class ActionWrapper {
	private HashMap<String, Action> mappings = new HashMap<>();

	public void bindActions(Class<?> clazz) {
		Request methodReq;
		Request classReq = clazz.getAnnotation(Request.class);
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			methodReq = method.getAnnotation(Request.class);
			if (methodReq != null) {
				add(ClassUtils.getUrlMapping(classReq, methodReq.value()), method);
			}
		}
	}

	private void add(String urlMapping, Method method) {
		mappings.put(urlMapping, new Action(method));
	}

	public Action getAction(String path) {
		return mappings.get(path);
	}

}
