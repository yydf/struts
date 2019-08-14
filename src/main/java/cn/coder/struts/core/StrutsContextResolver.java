package cn.coder.struts.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.aop.Aop;
import cn.coder.struts.aop.AopFactory;
import cn.coder.struts.support.StrutsLoader;

public final class StrutsContextResolver {
	private static final Logger logger = LoggerFactory.getLogger(StrutsContextResolver.class);

	private String encoding = "utf-8";
	private StrutsContext context;
	private ServletContext servletContext;
	private List<StrutsLoader> loaders;
	private Class<?>[] interceptors;
	private ActionHandler handler;
	private ViewHandler viewHandler;

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
	}

	private void initConfig() {
		try {
			InputStream input = StrutsContextResolver.class.getClassLoader().getResourceAsStream("struts.properties");
			if (input != null) {
				Properties p = new Properties();
				p.load(input);
				input.close();
				this.encoding = p.getProperty("encoding", "utf-8");
			}
		} catch (IOException e) {
			logger.error("Load struts.properties faild", e);
		}
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
		FilterRegistration registration = servletContext.getFilterRegistration("StrutsFilter");
		Class<?>[] controllers = context.getControllers();
		this.handler = new ActionHandler();
		this.handler.init(controllers, this.interceptors, registration);

		this.viewHandler = new ViewHandler(this.encoding);
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

	public synchronized void destroy() {
		this.encoding = null;
		this.servletContext = null;
		this.interceptors = null;
		this.handler.clear();
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
