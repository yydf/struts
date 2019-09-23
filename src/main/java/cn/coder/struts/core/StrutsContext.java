package cn.coder.struts.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import cn.coder.struts.aop.Aop;
import cn.coder.struts.support.ActionSupport;
import cn.coder.struts.support.Interceptor;
import cn.coder.struts.support.StrutsLoader;
import cn.coder.struts.support.SwaggerSupport;
import cn.coder.struts.util.BeanUtils;
import cn.coder.struts.util.ContextUtils;
import cn.coder.struts.wrapper.OrderWrapper;

public final class StrutsContext {
	private ServletContext servletContext;
	private final List<Class<?>> loaderClasses = new ArrayList<>();
	private final List<Class<?>> allClasses = new ArrayList<>();
	private final List<Class<?>> interceptors = new ArrayList<>();
	private final List<Class<?>> controllers = new ArrayList<>();
	private final List<Class<?>> swaggers = new ArrayList<>();

	public StrutsContext(ServletContext ctx) {
		this.servletContext = ctx;
	}

	public void scanPaths(String parent) {
		ContextUtils.scanPaths(this, parent);
	}

	public Set<String> getResourcePaths(String path) {
		return this.servletContext.getResourcePaths(path);
	}

	public void group(Class<?> clazz) {
		if (clazz != null) {
			if (StrutsLoader.class.isAssignableFrom(clazz))
				addClass(loaderClasses, clazz);
			else if (Interceptor.class.isAssignableFrom(clazz))
				addClass(interceptors, clazz);
			else if (ActionSupport.class.isAssignableFrom(clazz))
				addClass(controllers, clazz);
			else if (SwaggerSupport.class.isAssignableFrom(clazz))
				addClass(swaggers, clazz);
			else {

			}
			addClass(allClasses, clazz);
		}
	}

	private static void addClass(List<Class<?>> classes, Class<?> clazz) {
		if (!classes.contains(clazz))
			classes.add(clazz);
	}

	public void sortClass() {
		OrderWrapper.sort(loaderClasses);
		OrderWrapper.sort(interceptors);
	}

	public Class<?>[] getLoaderClass() {
		return BeanUtils.toArray(this.loaderClasses);
	}

	public Class<?>[] getAllClasses() {
		return BeanUtils.toArray(this.allClasses);
	}

	public Class<?>[] getInterceptors() {
		return BeanUtils.toArray(this.interceptors);
	}

	public Class<?>[] getControllers() {
		return BeanUtils.toArray(this.controllers);
	}

	public Class<?> getSwagger() {
		Class<?>[] arr = BeanUtils.toArray(this.swaggers);
		if (arr.length > 0)
			return arr[0];
		return null;
	}

	public synchronized void clear() {
		Aop.clear();
		this.servletContext = null;
		this.loaderClasses.clear();
		this.allClasses.clear();
		this.interceptors.clear();
		this.controllers.clear();
	}
}
