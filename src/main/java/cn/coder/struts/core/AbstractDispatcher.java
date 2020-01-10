package cn.coder.struts.core;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.handler.Handler;
import cn.coder.struts.handler.HandlerAdapter;
import cn.coder.struts.support.ServletWebRequest;
import cn.coder.struts.view.View;

public abstract class AbstractDispatcher {
	private static final Logger logger = LoggerFactory.getLogger(AbstractDispatcher.class);

	private static final String ATTRIBUTE_APPLICATION_CONTEXT = ApplicationContext.class.getName() + ".CONTEXT";
	
	protected ApplicationContext context;
	private ServletContext servletContext;

	public AbstractDispatcher(ServletContext servletContext) {
		createApplicationContext(servletContext);
	}

	private void createApplicationContext(ServletContext sc) {
		this.context = new ApplicationContext(sc);
		sc.setAttribute(ATTRIBUTE_APPLICATION_CONTEXT, this.context);
		this.servletContext = sc;
		this.context.doScan();
	}

	protected static void processError(ServletWebRequest req, Exception dispatchException) throws Exception {
		if (logger.isWarnEnabled())
			logger.warn("Dispatcher processed with error:", dispatchException);

		String errMsg = dispatchException.getMessage();
		Throwable cause = dispatchException.getCause();
		if (cause != null) {
			errMsg = cause.toString();
		}
		req.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errMsg);
	}

	protected static void processView(ServletWebRequest req, Object result, View[] currentViews)
			throws ServletException, Exception {
		if (result == null) {
			if (logger.isDebugEnabled())
				logger.debug("The result is null or void");
			return;
		}
		getView(result, currentViews).render(req, result);
	}

	protected static Handler getHandler(ServletWebRequest req, Handler[] currentHandlers) {
		for (Handler handler : currentHandlers) {
			if (handler.lookup(req))
				return handler;
		}
		return null;
	}

	protected static HandlerAdapter getHandlerAdapter(Handler handler, HandlerAdapter[] adapters)
			throws ServletException {
		for (HandlerAdapter adapter : adapters) {
			if (adapter.supports(handler))
				return adapter;
		}
		throw new ServletException("No HandlerAdapter found for handler '" + handler + "'");
	}

	protected static View getView(Object result, View[] views) throws ServletException {
		for (View view : views) {
			if (view.supports(result))
				return view;
		}
		throw new ServletException("No View found for result '" + result.getClass().getName() + "'");
	}

	protected void clear() {
		this.servletContext.removeAttribute(ATTRIBUTE_APPLICATION_CONTEXT);
		this.servletContext = null;
		if (this.context != null) {
			this.context.destroy();
			this.context = null;
		}
	}
}
