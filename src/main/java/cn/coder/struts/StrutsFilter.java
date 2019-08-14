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
import cn.coder.struts.core.ActionHandler;
import cn.coder.struts.core.StrutsContextResolver;
import cn.coder.struts.core.ViewHandler;

/**
 * 核心分发请求类 负责处理所有请求，返回相应的结果
 * 
 * @author YYDF
 *
 */
public final class StrutsFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(StrutsFilter.class);

	private String encoding;
	private ActionHandler actionHandler;
	private StrutsContextResolver resolver;
	private ViewHandler viewHandler;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		resolver = new StrutsContextResolver(filterConfig.getServletContext());
		resolver.init();
		resolver.start();
		this.encoding = resolver.getEncoding();
		this.actionHandler = resolver.getHandler();
		this.viewHandler = resolver.getViewHandler();
		if (logger.isDebugEnabled()) {
			long start = (long) filterConfig.getServletContext().getAttribute("__start");
			logger.debug("Struts framework started with {}ms", (System.currentTimeMillis() - start));
			StringBuilder sb = new StringBuilder();
			sb.append("\n===================    struts    ====================");
			sb.append("\nstruts.encoding                  ").append(this.encoding);
			sb.append("\nstruts.loaders                   ").append(resolver.getLoaderNum());
			sb.append("\nstruts.actions                   ").append(this.actionHandler.getActionNum());
			sb.append("\nstruts.interceptors              ").append(resolver.getInterceptorNum());
			logger.debug(sb.toString());
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		req.setCharacterEncoding(this.encoding);
		res.setCharacterEncoding(this.encoding);

		String path = req.getServletPath();
		Action action = this.actionHandler.getAction(path);
		if (action != null) {
			Object result = this.actionHandler.handle(action, req, res);
			if (result != null)
				this.viewHandler.handle(result, req, res);
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
		if (resolver != null) {
			resolver.destroy();
			resolver = null;
		}
		if (logger.isDebugEnabled())
			logger.debug("Struts destroied with {}ms", (System.currentTimeMillis() - start));
	}

}
