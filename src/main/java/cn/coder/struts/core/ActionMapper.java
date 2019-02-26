package cn.coder.struts.core;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.annotation.Request;
import cn.coder.struts.annotation.Request.HttpMethod;
import cn.coder.struts.support.ActionSupport;
import cn.coder.struts.util.FieldUtils;
import cn.coder.struts.wrapper.ResponseWrapper;

public class ActionMapper {

	private ActionSupport support;
	private final Class<?> clazz;
	private final HttpMethod httpMethod;
	private final Method method;
	private ResponseWrapper responseWrapper = new ResponseWrapper();

	public ActionMapper(Method method) {
		this.clazz = method.getDeclaringClass();
		this.method = method;
		if (method.getAnnotation(Request.class) != null)
			this.httpMethod = method.getAnnotation(Request.class).method();
		else
			this.httpMethod = null;
	}

	public void createBean(ArrayList<Class<?>> classes) throws ServletException {
		this.support = (ActionSupport) createBean(clazz, classes);
	}

	private static Object createBean(Class<?> class1, ArrayList<Class<?>> classes) throws ServletException {
		try {
			Object obj = class1.newInstance();
			if (obj != null) {
				Set<Field> fields = FieldUtils.getDeclaredFields(class1);
				for (Field field : fields) {
					if (field.getAnnotation(Resource.class) != null) {
						for (Class<?> cla : classes) {
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

	public void execute(HttpServletRequest req, HttpServletResponse res) throws ServletException {
		if (support == null)
			throw new NullPointerException("The controller can not be null");
		try {
			support.setRequest(req);
			support.setResponse(res);
			Object result = method.invoke(support);
			if (result != null) {
				responseWrapper.doResponse(result, req, res);
			}
			support.clear();
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException e) {
			throw new ServletException("Invoke method faild", e);
		}
	}

	public String getMethod() {
		if (httpMethod != null)
			return httpMethod.name();
		return null;
	}

}
