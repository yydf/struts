package cn.coder.struts.wrapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.support.ActionSupport;
import cn.coder.struts.util.Assert;
import cn.coder.struts.util.FieldUtils;

public class ActionWrapper {

	private final HashMap<String, Method> urlMappings = new HashMap<>();
	private final HashMap<Method, ActionSupport> cachedBeans = new HashMap<>();

	public void put(String urlMapping, Method method) {
		this.urlMappings.put(urlMapping, method);
	}

	public synchronized void clear() {
		urlMappings.clear();
		cachedBeans.clear();
	}

	public Set<String> getMappedUrls() {
		return urlMappings.keySet();
	}

	public synchronized void createBean(HashMap<Class<?>, Object> classes) throws ServletException {
		Method method;
		Set<String> keys = urlMappings.keySet();
		ActionSupport support;
		for (String key : keys) {
			method = urlMappings.get(key);
			support = (ActionSupport) createBean(method.getDeclaringClass(), classes);
			cachedBeans.put(method, support);
		}
	}

	private Object createBean(Class<?> clazz, HashMap<Class<?>, Object> classes) throws ServletException {
		try {
			Object obj = classes.get(clazz);
			if (obj == null) {
				obj = clazz.newInstance();
				classes.put(clazz, obj);
				Set<Field> fields = FieldUtils.getDeclaredFields(clazz);
				for (Field field : fields) {
					if (field.getAnnotation(Resource.class) != null) {
						Set<Class<?>> keys = classes.keySet();
						for (Class<?> cla : keys) {
							if (field.getType().isAssignableFrom(cla)) {
								try {
									FieldUtils.setValue(field, obj, createBean(cla, classes));
								} catch (SecurityException | SQLException e) {
									throw new ServletException("Create bean faild", e);
								}
								break;
							}
						}
					}
				}
			}
			return obj;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ServletException("Create controller faild", e);
		}
	}

	public Method getActionMethod(String servletPath) {
		return urlMappings.get(servletPath);
	}

	public Object execute(Method method, HttpServletRequest req, HttpServletResponse res) throws ServletException {
		ActionSupport support = cachedBeans.get(method);
		Assert.notNull(support, "controller");
		try {
			support.setRequest(req);
			support.setResponse(res);
			return method.invoke(support);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new ServletException("Invoke method faild", e);
		} finally {
			support.clear();
		}
	}

}
