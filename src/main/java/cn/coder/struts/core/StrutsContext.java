package cn.coder.struts.core;

import java.util.ArrayList;

import javax.servlet.ServletContext;

import cn.coder.struts.aop.Aop;
import cn.coder.struts.aop.AopFactory;
import cn.coder.struts.support.StrutsConfig;
import cn.coder.struts.wrapper.ActionWrapper;
import cn.coder.struts.wrapper.OrderWrapper;

public final class StrutsContext {

	private ServletContext servletContext;
	private ActionWrapper wrapper;
	private StrutsConfig strutsConfig;
	private ArrayList<Class<?>> handlers;

	public synchronized void init(ServletContext servletContext) {
		this.servletContext = servletContext;

		ininAopFactory();
		initConfig();
		initHandlers();
		initActions();
	}

	private void initConfig() {
		Class<?> clazz = (Class<?>) servletContext.getAttribute("StrutsConfig");
		servletContext.removeAttribute("StrutsConfig");
		if (clazz != null) {
			this.strutsConfig = (StrutsConfig) Aop.create(clazz);
		}
	}

	private void ininAopFactory() {
		@SuppressWarnings("unchecked")
		ArrayList<Class<?>> classes = (ArrayList<Class<?>>) servletContext.getAttribute("Classes");
		servletContext.removeAttribute("Classes");
		AopFactory.init(classes);
	}

	private void initHandlers() {
		@SuppressWarnings("unchecked")
		ArrayList<Class<?>> classes = (ArrayList<Class<?>>) servletContext.getAttribute("Handlers");
		servletContext.removeAttribute("Handlers");
		OrderWrapper.sort(classes);
//		if (classes == null) {
//			classes = new ArrayList<>();
//		}
//		classes.add(ActionHandler.class);
		this.handlers = classes;
	}

	private void initActions() {
		@SuppressWarnings("unchecked")
		ArrayList<Class<?>> classes = (ArrayList<Class<?>>) servletContext.getAttribute("Controllers");
		servletContext.removeAttribute("Controllers");
		@SuppressWarnings("unchecked")
		ArrayList<Class<?>> interceptors = (ArrayList<Class<?>>) servletContext.getAttribute("Interceptors");
		servletContext.removeAttribute("Interceptors");
		if (interceptors != null) {
			OrderWrapper.sort(interceptors);
		}
		this.wrapper = new ActionWrapper(classes, interceptors);
	}

	public ActionWrapper getWrapper() {
		return this.wrapper;
	}

	public ArrayList<Class<?>> getHandlers() {
		return this.handlers;
	}

	public synchronized void start() {
		if (this.strutsConfig != null) {
			try {
				this.strutsConfig.onStartup(servletContext);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	public synchronized void destroy() {
		if (this.strutsConfig != null) {
			try {
				this.strutsConfig.destroy();
			} catch (Exception e) {
				// TODO: handle exception
			}
			this.strutsConfig = null;
		}
		this.wrapper.clear();
		this.wrapper = null;
	}

}