package cn.coder.struts;

import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.event.ServletRequestHandleEvent;
import cn.coder.struts.handler.Handler;
import cn.coder.struts.handler.HandlerAdapter;
import cn.coder.struts.view.View;

public final class StrutsFilter extends AbstractStrutsFilter {
	private static final Logger logger = LoggerFactory.getLogger(StrutsFilter.class);

	@Override
	protected void doDispatch(long startTime, HttpServletRequest request, HttpServletResponse response) throws ServletException {
		if (logger.isDebugEnabled()) {
			logger.debug("URIDispatcher processing {} request for [{}]", request.getMethod(), request.getRequestURI());
			Enumeration<String> attrNames = request.getParameterNames();
			while (attrNames.hasMoreElements()) {
				String attrName = attrNames.nextElement();
				logger.debug("Parameter:[{}] {}", attrName, request.getParameter(attrName));
			}
		}

		Handler handler = null;
		Object result = null;
		Exception dispatchException = null;

		try {
			checkMultipart(request);

			handler = getHandler(request);
			if (handler == null) {
				if (logger.isDebugEnabled())
					logger.debug("No handler for request [{}]", request.getRequestURI());
				return;
			}

			if (!handler.preHandle(request, response)) {
				if (logger.isDebugEnabled())
					logger.debug("Dispatcher stoped by '{}' preHandle", handler);
				return;
			}

			try {
				HandlerAdapter adapter = getHandlerAdapter(handler);
				result = adapter.handle(request, response, handler);
			} catch (Exception e) {
				dispatchException = e;
			}

			// 处理异常和返回值
			processResultView(request, response, result, dispatchException);

		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			if (handler != null) {
				handler.finish(request, response, result, dispatchException);
			}
			publishEvent(new ServletRequestHandleEvent(this, request, response, startTime, result, dispatchException));
			clearMultipart(request);
		}
	}

	private void processResultView(HttpServletRequest req, HttpServletResponse res, Object result, Exception error)
			throws ServletException, Exception {
		if (error != null) {
			if (logger.isWarnEnabled())
				logger.warn("Dispatcher processed with error:", error);
			String errMsg = error.getCause() != null ? error.getCause().toString() : error.getMessage();
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errMsg);
			return;
		}

		if (result == null) {
			if (logger.isDebugEnabled())
				logger.debug("The result is null or void");
			return;
		}
		View view = getView(result);
		view.render(result, req, res);
	}
}
