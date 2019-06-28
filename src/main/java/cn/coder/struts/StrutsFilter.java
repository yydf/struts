package cn.coder.struts;

import java.io.IOException;

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

import cn.coder.struts.core.Action;
import cn.coder.struts.core.StrutsDispatcher;
import cn.coder.struts.core.ActionHandler;

public final class StrutsFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(StrutsFilter.class);

	private StrutsDispatcher dispatcher;
	private ActionHandler actionHandler;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		long start = System.currentTimeMillis();
		dispatcher = new StrutsDispatcher(filterConfig.getServletContext());
		dispatcher.start();
		this.actionHandler = dispatcher.getHandler();
		if (logger.isDebugEnabled())
			logger.debug("Struts started with {}ms", (System.currentTimeMillis() - start));
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		req.setCharacterEncoding("utf-8");

		String path = req.getServletPath();
		Action action = this.actionHandler.getAction(path);
		if (action != null) {
			if (!req.getMethod().equals(action.getHttpMethod())) {
				if (logger.isDebugEnabled())
					logger.debug("Action '{}' not allowed '{}'", path, req.getMethod());
				res.sendError(405);
				return;
			}
			this.actionHandler.handle(action, req, res);
		} else {
			if (logger.isWarnEnabled())
				logger.warn("Not found the path '{}'", path);
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		long start = System.currentTimeMillis();
		this.actionHandler = null;
		if (dispatcher != null) {
			dispatcher.destroy();
			dispatcher = null;
		}
		if (logger.isDebugEnabled())
			logger.debug("Struts destroied with {}ms", (System.currentTimeMillis() - start));
	}

}
