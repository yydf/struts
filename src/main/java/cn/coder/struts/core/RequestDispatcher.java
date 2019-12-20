package cn.coder.struts.core;

import java.io.PrintWriter;
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

public class RequestDispatcher extends AbstractDispatcher {
	private static final Logger logger = LoggerFactory.getLogger(RequestDispatcher.class);

	private List<Handler> handlers;
	private List<HandlerAdapter> handlerAdapters;
	private List<View> views;
	private List<Method> destroyMethods;

	public RequestDispatcher(FilterConfig filterConfig) {
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
			logger.debug(
					"RequestDispatcher processing " + req.getMethod() + " request for [" + req.getRequestURI() + "]");

		try {
			Handler handler = getHandler(req);
			if (handler == null) {
				noHandlerFound(req);
				return;
			}

			if (!handler.preHandle(req)) {
				return;
			}

			Object result = null;
			Exception dispatchException = null;

			try {
				HandlerAdapter adapter = getHandlerAdapter(handler);
				result = adapter.handle(req, handler);
			} catch (Exception e) {
				dispatchException = e;
			}

			// 处理返回值
			processDispatchResult(req, result, dispatchException);

			handler.finishHandle(req, result, dispatchException);
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			if (req.isMultipartRequest()) {
				req.clear();
			}
		}
	}

	private void processDispatchResult(ServletWebRequest req, Object result, Exception dispatchException)
			throws Exception {
		if (dispatchException != null) {
			PrintWriter pw = req.getWriter();
			dispatchException.printStackTrace(pw);
			pw.close();
		} else {
			if (result == null)
				return;
			View view = getView(result);
			view.render(req, result);
		}
	}

	private Handler getHandler(ServletWebRequest req) {
		for (Handler handler : this.handlers) {
			if (handler.lookup(req))
				return handler;
		}
		return null;
	}

	private HandlerAdapter getHandlerAdapter(Handler handler) throws ServletException {
		for (HandlerAdapter adapter : handlerAdapters) {
			if (adapter.supports(handler))
				return adapter;
		}
		throw new ServletException("No HandlerAdapter found for handler '" + handler + "'");
	}

	private View getView(Object result) throws ServletException {
		for (View view : this.views) {
			if (view.supports(result))
				return view;
		}
		throw new ServletException("No View found for result '" + result.getClass().getName() + "'");
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
