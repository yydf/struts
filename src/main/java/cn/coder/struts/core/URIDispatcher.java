package cn.coder.struts.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.annotation.WebInitializer;
import cn.coder.struts.handler.Handler;
import cn.coder.struts.handler.HandlerAdapter;
import cn.coder.struts.support.ServletWebRequest;
import cn.coder.struts.view.View;
import cn.coder.struts.wrapper.MultipartRequestWrapper.processFile;

public class URIDispatcher extends AbstractDispatcher {
	private static final Logger logger = LoggerFactory.getLogger(URIDispatcher.class);

	private Handler[] handlers;
	private HandlerAdapter[] handlerAdapters;
	private View[] views;
	private Method[] destroyMethods;
	private processFile process;

	public URIDispatcher(FilterConfig filterConfig) {
		super(filterConfig.getServletContext());
	}

	public synchronized void init() {
		initHandlers();
		initHandlerAdapters();
		initViews();
		runWebInitializer();
		initFileProcess();
	}

	private void initHandlers() {
		this.handlers = this.context.getHandlers();
	}

	private void initHandlerAdapters() {
		this.handlerAdapters = this.context.getHandlerAdapters();
	}

	private void initViews() {
		this.views = this.context.getViews();
	}

	private void initFileProcess() {
		this.process = this.context.getFileProcess();
	}

	private void runWebInitializer() {
		Class<?>[] initClasses = this.context.getClasses(WebInitializer.class);
		if (initClasses.length > 0) {
			WebInitializer init;
			ArrayList<Method> list = new ArrayList<>();
			for (Class<?> clazz : initClasses) {
				try {
					init = clazz.getAnnotation(WebInitializer.class);
					Method m = clazz.getDeclaredMethod(init.init());
					m.invoke(this.context.getSingleton(clazz.getName()));
					list.add(clazz.getDeclaredMethod(init.destroy()));
				} catch (Exception e) {
					logger.warn("Init for class '" + clazz + "' faild.", e);
				}
			}
			Method[] temp = new Method[list.size()];
			this.destroyMethods = list.toArray(temp);
		}
	}

	public boolean doDispatch(ServletRequest request, ServletResponse response) throws ServletException {
		final ServletWebRequest req = new ServletWebRequest(request, response, this.process);
		if (logger.isDebugEnabled()) {
			logger.debug("URIDispatcher processing " + req.getMethod() + " request for [" + req.getRequestURI() + "]");
			Enumeration<?> attrNames = req.getParameterNames();
			while (attrNames.hasMoreElements()) {
				String attrName = (String) attrNames.nextElement();
				logger.debug("Parameter:[{}] {}", attrName, req.getParameter(attrName));
			}
		}

		try {
			Handler handler = getHandler(req, this.handlers);
			if (handler == null) {
				if (logger.isDebugEnabled()) 
					logger.debug("No handler found for request [{}]", req.getRequestURI());
				return false;
			}

			if (!handler.preHandle(req)) {
				if (logger.isDebugEnabled())
					logger.debug("Dispatcher stoped by '{}' preHandle", handler);
				return true;
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
			req.clear();
		}
		return true;
	}

	public synchronized void clear() {
		this.handlers = null;
		this.handlerAdapters = null;
		if (this.destroyMethods != null && this.destroyMethods.length > 0) {
			Class<?> declaringClass;
			for (Method method : this.destroyMethods) {
				declaringClass = method.getDeclaringClass();
				try {
					method.invoke(this.context.getSingleton(declaringClass));
				} catch (Exception e) {
					logger.warn("Destroy for class '" + declaringClass + "' faild.", e);
				}
			}
			this.destroyMethods = null;
		}
		super.clear();
		if (logger.isDebugEnabled())
			logger.debug("URIDispatcher cleared");
	}

}
