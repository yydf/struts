package cn.coder.struts.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.annotation.WebInitializer;
import cn.coder.struts.handler.Handler;
import cn.coder.struts.handler.HandlerAdapter;
import cn.coder.struts.support.ServletWebRequest;
import cn.coder.struts.view.View;

public class URIDispatcher extends AbstractDispatcher {
	private static final Logger logger = LoggerFactory.getLogger(URIDispatcher.class);

	private List<Handler> handlers;
	private List<HandlerAdapter> handlerAdapters;
	private List<View> views;
	private List<Method> destroyMethods;

	public URIDispatcher(FilterConfig filterConfig) {
		super(filterConfig.getServletContext());
	}

	public synchronized void init() {
		initHandlers();
		initHandlerAdapters();
		initViews();
		runWebInitializer();
	}

	private void initHandlers() {
		this.handlers = new ArrayList<>();
		this.handlers.addAll(this.context.getHandlers());
	}

	private void initHandlerAdapters() {
		this.handlerAdapters = new ArrayList<>();
		this.handlerAdapters.addAll(this.context.getHandlerAdapters());
	}

	private void initViews() {
		this.views = new ArrayList<>();
		this.views.addAll(this.context.getViews());
	}

	private void runWebInitializer() {
		this.destroyMethods = new ArrayList<>();
		Class<?>[] classes = this.context.getClasses();
		WebInitializer init;
		for (Class<?> clazz : classes) {
			init = clazz.getAnnotation(WebInitializer.class);
			if (init != null) {
				try {
					Method m = clazz.getDeclaredMethod(init.init());
					m.invoke(this.context.getSingleton(clazz.getName()));
					this.destroyMethods.add(clazz.getDeclaredMethod(init.destroy()));
				} catch (Exception e) {
					logger.warn("Init for class '" + clazz + "' faild.", e);
				}
			}
		}
	}

	public void doDispatch(ServletWebRequest req) throws ServletException {
		if (logger.isDebugEnabled())
			logger.debug("URIDispatcher processing " + req.getMethod() + " request for [" + req.getRequestURI() + "]");

		try {
			Handler handler = getHandler(req, this.handlers);
			if (handler == null) {
				noHandlerFound(req);
				return;
			}

			if (!handler.preHandle(req)) {
				if (logger.isDebugEnabled())
					logger.debug("Dispatcher stoped by '{}' preHandle", handler);
				return;
			}

			Object result = null;
			Exception dispatchException = null;

			try {
				HandlerAdapter adapter = getHandlerAdapter(handler, this.handlerAdapters);
				result = adapter.handle(req, handler);
			} catch (Exception e) {
				dispatchException = e;
			}

			// 处理异常和返回值
			if (dispatchException != null)
				processError(req, dispatchException);
			else
				processView(req, result, this.views);

			handler.finishHandle(req, result, dispatchException);
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			if (req.isMultipartRequest()) {
				req.clear();
			}
		}
	}

	public synchronized void clear() {
		this.handlers.clear();
		this.handlers = null;
		this.handlerAdapters.clear();
		this.handlerAdapters = null;
		for (Method method : this.destroyMethods) {
			try {
				method.invoke(this.context.getSingleton(method.getDeclaringClass().getName()));
			} catch (Exception e) {
				logger.warn("Destroy for class '" + method.getDeclaringClass() + "' faild.", e);
			}
		}
		this.destroyMethods.clear();
		this.destroyMethods = null;
	}

}
