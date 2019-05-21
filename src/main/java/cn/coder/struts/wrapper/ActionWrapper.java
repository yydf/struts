package cn.coder.struts.wrapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.support.ActionSupport;
import cn.coder.struts.util.Assert;
import cn.coder.struts.util.BeanUtils;

public class ActionWrapper {

	private final ArrayList<Method> startUpMappings = new ArrayList<>();
	private final HashMap<String, Method> urlMappings = new HashMap<>();
	private final HashMap<Method, ActionSupport> cachedBeans = new HashMap<>();

	public void put(String urlMapping, Method method) {
		this.urlMappings.put(urlMapping, method);
	}

	public void add(Method method) {
		this.startUpMappings.add(method);
	}

	public synchronized void clear() {
		startUpMappings.clear();
		urlMappings.clear();
		cachedBeans.clear();
	}

	public Set<String> getMappedUrls() {
		return urlMappings.keySet();
	}

	public void registerBean(String action, HashMap<Class<?>, Object> classes) throws ServletException {
		Method method = urlMappings.get(action);
		cachedBeans.put(method, (ActionSupport) createBean(method.getDeclaringClass(), classes));
	}

	public void runStartUp(HashMap<Class<?>, Object> classes) throws ServletException {
		if (!this.startUpMappings.isEmpty()) {
			Object obj;
			for (Method method : this.startUpMappings) {
				obj = createBean(method.getDeclaringClass(), classes);
				try {
					method.invoke(obj);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new ServletException("Run startup faild", e);
				}
			}
		}
	}

	private synchronized Object createBean(Class<?> clazz, HashMap<Class<?>, Object> classes) throws ServletException {
		try {
			Object obj = classes.get(clazz);
			if (obj == null) {
				obj = clazz.newInstance();
				classes.put(clazz, obj);
				Set<Field> fields = BeanUtils.getDeclaredFields(clazz);
				for (Field field : fields) {
					if (field.getAnnotation(Resource.class) != null) {
						Set<Class<?>> keys = classes.keySet();
						for (Class<?> cla : keys) {
							if (field.getType().isAssignableFrom(cla)) {
								try {
									BeanUtils.setValue(field, obj, createBean(cla, classes));
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
