package cn.coder.struts.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import cn.coder.struts.aop.Aop;
import cn.coder.struts.aop.AopFactory;
import cn.coder.struts.support.StrutsLoader;

public final class StrutsContextResolver {

	private StrutsContext context;
	private ServletContext servletContext;
	private List<StrutsLoader> loaders;
	private Class<?>[] interceptors;
	private ActionHandler handler;

	public StrutsContextResolver(ServletContext ctx) {
		this.servletContext = ctx;
		this.context = (StrutsContext) ctx.getAttribute("StrutsContext");
	}

	public synchronized void init() {
		initAop();
		initLoader();
		initInterceptor();
		initHandler();
	}

	private void initAop() {
		AopFactory.init(context.getAllClasses());
	}

	private void initLoader() {
		Class<?>[] loaderClass = context.getLoaderClass();
		if (loaderClass.length > 0) {
			this.loaders = new ArrayList<>(loaderClass.length);
			for (int i = 0; i < loaderClass.length; i++) {
				this.loaders.add((StrutsLoader) Aop.create(loaderClass[i]));
			}
		}
	}

	private void initInterceptor() {
		this.interceptors = context.getInterceptors();
	}

	private void initHandler() {
		this.handler = new ActionHandler(context.getControllers());
		this.handler.buildInterceptors(this.interceptors);
		this.handler.registerPath(servletContext.getFilterRegistration("StrutsFilter"));
	}

	public synchronized void start() {
		if (loaders != null) {
			for (StrutsLoader loader : loaders) {
				loader.load();
			}
		}
	}

	public ActionHandler getHandler() {
		return this.handler;
	}

	public synchronized void destroy() {
		this.servletContext = null;
		this.interceptors = null;
		this.handler.clear();
		if (this.loaders != null) {
			for (StrutsLoader loader : loaders) {
				loader.destroy();
			}
			this.loaders.clear();
		}
		if (this.context != null) {
			this.context.clear();
			this.context = null;
		}
	}

}
