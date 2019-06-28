package cn.coder.struts.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import cn.coder.struts.support.ActionSupport;
import cn.coder.struts.support.Interceptor;
import cn.coder.struts.support.StrutsLoader;

public final class StrutsContext {

	private ServletContext servletContext;
	private Class<?> loaderClass;
	private Set<Class<?>> allClasses = new HashSet<>();// 避免重复
	private Set<Class<?>> interceptors = new HashSet<>();// 避免重复
	private Set<Class<?>> controllers = new HashSet<>();// 避免重复

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
					this.loaderClass = clazz;
				else if (Interceptor.class.isAssignableFrom(clazz))
					interceptors.add(clazz);
				else if (ActionSupport.class.isAssignableFrom(clazz))
					controllers.add(clazz);
				else {

				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Class<?> getLoaderClass() {
		return this.loaderClass;
	}

	public ArrayList<Class<?>> getAllClasses() {
		return new ArrayList<>(this.allClasses);
	}

	public ArrayList<Class<?>> getInterceptors() {
		return new ArrayList<>(this.interceptors);
	}

	public ArrayList<Class<?>> getControllers() {
		return new ArrayList<>(this.controllers);
	}

	public synchronized void clear() {
		this.servletContext = null;
		this.loaderClass = null;
		this.allClasses.clear();
		this.interceptors.clear();
		this.controllers.clear();
	}

}
