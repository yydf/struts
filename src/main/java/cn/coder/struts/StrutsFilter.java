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

import cn.coder.struts.core.ActionMapper;
import cn.coder.struts.core.StrutsContext;

public class StrutsFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(StrutsFilter.class);
	private StrutsContext context = new StrutsContext();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		context.init(filterConfig.getServletContext());
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		long start = System.currentTimeMillis();
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		HttpServletRequest req = (HttpServletRequest) request;
		if (logger.isDebugEnabled())
			logger.debug("Request [{}]{}", req.getMethod(), req.getServletPath());
		if (req.getMethod().equals("OPTIONS")) {
			chain.doFilter(request, response);
			return;
		}
		ActionMapper action = context.findAction(req.getServletPath());
		if (action != null) {
			HttpServletResponse res = (HttpServletResponse) response;
			if (!req.getMethod().equals(action.getMethod())) {
				res.sendError(405, "Request method '" + req.getMethod() + "' not supported");
				if (logger.isDebugEnabled())
					logger.debug("{} method not allowed", req.getMethod());
				return;
			}
			if (!context.checkFilter(req, res)) {
				if (logger.isDebugEnabled())
					logger.debug("Action stoped");
				return;
			}
			action.execute(req, res);
			if (logger.isDebugEnabled())
				logger.debug("Request finished with {}ms", (System.currentTimeMillis() - start));
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		context.destroy();
		context = null;
	}

}
