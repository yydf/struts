package cn.coder.struts.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.aop.Aop;
import cn.coder.struts.support.ActionSupport;
import cn.coder.struts.support.Interceptor;
import cn.coder.struts.support.StrutsLoader;

public final class StrutsContext {
	private static final Logger logger = LoggerFactory.getLogger(StrutsContext.class);

	private ServletContext servletContext;
	private final List<Class<?>> loaderClasses = new ArrayList<>();
	private final List<Class<?>> allClasses = new ArrayList<>();
	private final List<Class<?>> interceptors = new ArrayList<>();
	private final List<Class<?>> controllers = new ArrayList<>();

	public StrutsContext(ServletContext ctx) {
		this.servletContext = ctx;
	}

	public Set<String> getResourcePaths(String path) {
		return this.servletContext.getResourcePaths(path);
	}

	public void split(String className) {
		try {
			Class<?> clazz = Class.forName(className);
			if (clazz != null) {
				allClasses.add(clazz);
				if (StrutsLoader.class.isAssignableFrom(clazz))
					addClass(loaderClasses, clazz);
				else if (Interceptor.class.isAssignableFrom(clazz))
					addClass(interceptors, clazz);
				else if (ActionSupport.class.isAssignableFrom(clazz))
					addClass(controllers, clazz);
				else {

				}
				addClass(allClasses, clazz);
			}
		} catch (ClassNotFoundException e) {
			if (logger.isErrorEnabled())
				logger.error("Path '{}' not found", className);
		}
	}

	private static void addClass(List<Class<?>> classes, Class<?> clazz) {
		if (!classes.contains(clazz))
			classes.add(clazz);
	}

	public List<Class<?>> getLoaderClass() {
		return this.loaderClasses;
	}

	public List<Class<?>> getAllClasses() {
		return this.allClasses;
	}

	public List<Class<?>> getInterceptors() {
		return this.interceptors;
	}

	public List<Class<?>> getControllers() {
		return this.controllers;
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
