package cn.coder.struts;

import java.io.IOException;
import java.util.ArrayList;

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

import cn.coder.struts.core.Handlers;
import cn.coder.struts.core.StrutsContext;
import cn.coder.struts.view.Action;
import cn.coder.struts.wrapper.ActionWrapper;

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
	private StrutsContext context;
	private ActionWrapper actionWrapper;
	private ArrayList<Class<?>> handlerStack;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		long start = System.currentTimeMillis();
		context = new StrutsContext();
		context.init(filterConfig.getServletContext());
		context.start();
		actionWrapper = context.getWrapper();
		handlerStack = context.getHandlers();
		if (logger.isDebugEnabled())
			logger.debug("Struts context started with {} ms", (System.currentTimeMillis() - start));
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		req.setCharacterEncoding("utf-8");

		Handlers handlers = new Handlers(handlerStack, req, res);
		if (!handlers.complete()) {
			if (logger.isDebugEnabled())
				logger.debug("Handler stoped");
			return;
		}

		Action action = actionWrapper.getAction(req);
		if (action != null) {
			actionWrapper.execute(action, req, res);
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		if (handlerStack != null) {
			handlerStack.clear();
			handlerStack = null;
		}
		if (context != null) {
			context.destroy();
			context = null;
		}
	}

}
