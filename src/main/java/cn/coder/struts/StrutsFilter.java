package cn.coder.struts;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.core.Action;
import cn.coder.struts.core.ActionHandler;
import cn.coder.struts.core.StrutsContext;

/**
 * 核心控制类<br>
 * 包括初始化Action和注入@Resource对象<br>
 * 初始化ResponseWrapper
 * 
 * @author YYDF
 *
 */
public class StrutsFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(StrutsFilter.class);
	private StrutsContext context = new StrutsContext();
	private ActionHandler handler;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		long start = System.currentTimeMillis();
		ServletContext servletContext = filterConfig.getServletContext();
		context.init(servletContext);
		context.startUp(servletContext);
		handler = context.getHandler();
		if (logger.isDebugEnabled())
			logger.debug("Struts context started with {} ms", (System.currentTimeMillis() - start));
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		req.setCharacterEncoding("UTF-8");
		res.setCharacterEncoding("UTF-8");

		if ("OPTIONS".equals(req.getMethod())) {
			chain.doFilter(request, response);
			return;
		}

		String servletPath = req.getServletPath();
		Action action = handler.getAction(servletPath);
		if (action != null) {
			if (logger.isDebugEnabled())
				logger.debug("Find the path '{}'", servletPath);
			handler.handle(action, req, res);
		} else {
			if (logger.isDebugEnabled())
				logger.debug("Not found the path '{}'", servletPath);
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		context.destroy();
		if (logger.isDebugEnabled())
			logger.debug("Struts context destroied");
	}

}
