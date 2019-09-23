package cn.coder.struts.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;

import cn.coder.struts.aop.Aop;
import cn.coder.struts.aop.AopFactory;
import cn.coder.struts.support.StrutsLoader;
import cn.coder.struts.util.Streams;
import cn.coder.struts.wrapper.SwaggerWrapper;

public final class StrutsContextResolver {
	private String encoding = "utf-8";
	private StrutsContext context;
	private ServletContext servletContext;
	private List<StrutsLoader> loaders;
	private Class<?>[] interceptors;
	private FilterRegistration registration;
	private ActionHandler handler;
	private ViewHandler viewHandler;
	private SwaggerWrapper swaggerWrapper;

	public StrutsContextResolver(ServletContext ctx) {
		this.servletContext = ctx;
		this.context = (StrutsContext) ctx.getAttribute("StrutsContext");
	}

	public synchronized void init() {
		initConfig();
		initAop();
		initLoader();
		initInterceptor();
		initHandler();
		initSwagger();
	}

	private void initConfig() {
		Properties p = Streams.loadProperties("struts.properties");
		this.encoding = p.getProperty("encoding", "utf-8");
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
		this.registration = servletContext.getFilterRegistration("StrutsFilter");
		Class<?>[] controllers = context.getControllers();
		this.handler = new ActionHandler();
		this.handler.init(controllers, this.interceptors, registration);

		this.viewHandler = new ViewHandler(this.encoding);
	}

	private void initSwagger() {
		Class<?> swaggerClazz = context.getSwagger();
		if (swaggerClazz != null) {
			String templete = Streams.asString("swagger.tpl");
			this.swaggerWrapper = new SwaggerWrapper(swaggerClazz, templete, this.handler);
			this.handler.bindSwagger(registration, swaggerWrapper.getRequestUrl());
		}
	}

	public synchronized void start() {
		if (loaders != null) {
			for (StrutsLoader loader : loaders) {
				loader.load();
			}
		}
	}

	public String getEncoding() {
		return this.encoding;
	}

	public int getLoaderNum() {
		return this.loaders == null ? 0 : this.loaders.size();
	}

	public int getInterceptorNum() {
		return this.interceptors.length;
	}

	public ActionHandler getHandler() {
		return this.handler;
	}

	public ViewHandler getViewHandler() {
		return this.viewHandler;
	}

	public SwaggerWrapper getSwaggerWrapper() {
		return this.swaggerWrapper;
	}

	public synchronized void destroy() {
		this.encoding = null;
		this.servletContext = null;
		this.interceptors = null;
		this.registration = null;
		this.handler.clear();
		if (this.swaggerWrapper != null) {
			this.swaggerWrapper.clear();
			this.swaggerWrapper = null;
		}
		this.viewHandler = null;
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
