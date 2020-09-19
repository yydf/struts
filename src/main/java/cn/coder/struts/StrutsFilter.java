package cn.coder.struts;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.event.ServletRequestHandleEvent;
import cn.coder.struts.handler.HandlerAdapter;
import cn.coder.struts.handler.SimpleExecutor;

public final class StrutsFilter extends AbstractStrutsFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(StrutsFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if (logger.isDebugEnabled())
			logger.debug("Starting init content");
		super.initContext(filterConfig);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		long startTime = System.currentTimeMillis();

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		super.dispatch(startTime, req, res);
	}

	@Override
	protected void doDispatch(long startTime, HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
		if (logger.isDebugEnabled()) {
			logger.debug("Processing {} request for [{}]", request.getMethod(), request.getRequestURI());
			Enumeration<String> attrNames = request.getParameterNames();
			while (attrNames.hasMoreElements()) {
				String attrName = attrNames.nextElement();
				logger.debug("Parameter:[{}] {}", attrName, request.getParameter(attrName));
			}
		}

		SimpleExecutor executor = null;
		Object result = null;
		Throwable dispatchException = null;

		try {
			checkMultipart(request);

			executor = getExecutor(request);
			if (executor == null || executor.getHandler() == null) {
				if (logger.isDebugEnabled())
					logger.debug("No handler for request [{}]", request.getRequestURI());
				return;
			}

			try {
				if (!executor.checkBefore(request, response)) {
					if (logger.isDebugEnabled())
						logger.debug("Request stoped by '{}' executor", executor);
					return;
				}
				
				HandlerAdapter handlerAdapter = getHandlerAdapter(executor.getHandler());
				result = handlerAdapter.handle(request, response, executor.getHandler());
				
				executor.doAfter(result);
			} catch (Throwable e) {
				dispatchException = e;
			}

			// 处理异常和返回值
			processResultView(request, response, result, dispatchException);

		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			// if (handler != null) {
			// handler.finish(request, response, result, dispatchException);
			// }
			publishEvent(new ServletRequestHandleEvent(this, request, response, startTime, result, dispatchException));
			clearMultipart(request);
		}
	}

	private void processResultView(HttpServletRequest req, HttpServletResponse res, Object result, Throwable dispatchError)
			throws ServletException, Exception {
		if (dispatchError != null) {
			logger.warn("Processed with error:", dispatchError);
			String errMsg = dispatchError.getCause() != null ? dispatchError.getCause().toString() : dispatchError.getMessage();
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errMsg);
			return;
		}

		if (result == null) {
			if (logger.isDebugEnabled())
				logger.debug("The result is null or void");
			return;
		}
		getView(result).render(result, req, res);
	}

	@Override
	public void destroy() {
		if (logger.isDebugEnabled())
			logger.debug("Starting destroy content");
		super.clear();
	}
}
