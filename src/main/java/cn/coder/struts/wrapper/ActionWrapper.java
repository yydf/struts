package cn.coder.struts.wrapper;

import java.lang.reflect.Method;
import java.util.HashMap;

import cn.coder.struts.annotation.Request;
import cn.coder.struts.core.Action;
import cn.coder.struts.util.ClassUtils;

/**
 * Action的处理类，包含所有Action集合
 * 
 * @author YYDF
 *
 */
public final class ActionWrapper {
	private final HashMap<String, Action> mappings = new HashMap<>();

	public void bindActions(Class<?> clazz) {
		Request req;
		Request classReq = clazz.getAnnotation(Request.class);
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			req = method.getAnnotation(Request.class);
			if (req != null) {
				add(ClassUtils.getUrlMapping(classReq, req.value()), method);
			}
		}
	}

	private void add(String urlMapping, Method method) {
		mappings.put(urlMapping, new Action(method));
	}

	public Action getAction(String path) {
		return mappings.get(path);
	}

	public synchronized void clear() {
		mappings.clear();
	}

}
