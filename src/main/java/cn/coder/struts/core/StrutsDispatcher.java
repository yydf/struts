package cn.coder.struts.core;

import java.util.ArrayList;

import javax.servlet.ServletContext;

import cn.coder.struts.aop.Aop;
import cn.coder.struts.aop.AopFactory;
import cn.coder.struts.support.StrutsLoader;

public final class StrutsDispatcher {

	private StrutsContext context;
	private ServletContext servletContext;
	private StrutsLoader loader;
	private ArrayList<Class<?>> interceptors;
	private ActionHandler handler;

	public StrutsDispatcher(ServletContext ctx) {
		this.servletContext = ctx;
		this.context = (StrutsContext) ctx.getAttribute("StrutsContext");

		initAop();
		initLoader();
		initInterceptor();
		initMapping();
	}

	private void initAop() {
		AopFactory.init(context.getAllClasses());
	}

	private void initLoader() {
		this.loader = (StrutsLoader) Aop.create(context.getLoaderClass());
	}

	private void initInterceptor() {
		this.interceptors = context.getInterceptors();
	}

	private void initMapping() {
		this.handler = new ActionHandler(context.getControllers());
		this.handler.buildInterceptors(this.interceptors);
		this.handler.registerPath(servletContext.getFilterRegistration("StrutsFilter"));
	}

	public void start() {
		if (loader != null) {
			loader.onStartup(this.servletContext);
		}
	}

	public ActionHandler getHandler() {
		return this.handler;
	}

	public void destroy() {
		this.servletContext = null;
		this.interceptors.clear();
		this.handler.clear();
		if (this.loader != null) {
			loader.destroy();
			this.loader = null;
		}
		if (this.context != null) {
			this.context.clear();
			this.context = null;
		}
	}

}
