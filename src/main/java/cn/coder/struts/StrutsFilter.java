package cn.coder.struts;

import java.io.IOException;
import java.lang.reflect.Method;

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

import cn.coder.struts.util.ThreadEx;
import cn.coder.struts.util.ClassUtils;
import cn.coder.struts.wrapper.ActionWrapper;
import cn.coder.struts.wrapper.ResponseWrapper;
import cn.coder.struts.wrapper.WebContextWrapper;

public class StrutsFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(StrutsFilter.class);
	private WebContextWrapper context = new WebContextWrapper();
	private ResponseWrapper wrapper = new ResponseWrapper();
	private ActionWrapper actionWrapper = new ActionWrapper();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		context.init(filterConfig.getServletContext(), actionWrapper);
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
		Method method = actionWrapper.getActionMethod(req.getServletPath());
		if (method != null) {
			HttpServletResponse res = (HttpServletResponse) response;
			if (!ClassUtils.allowHttpMethod(method, req.getMethod())) {
				res.sendError(405, "Request method '" + req.getMethod() + "' not supported");
				if (logger.isDebugEnabled())
					logger.debug("{} method not allowed", req.getMethod());
				return;
			}
			if (!context.checkFilter(req, res)) {
				if (logger.isDebugEnabled())
					logger.debug("Action stoped by filter");
				return;
			}
			Object result = actionWrapper.execute(method, req, res);
			if (result != null) {
				wrapper.doResponse(result, req, res);
			}
		} else {
			chain.doFilter(request, response);
		}
		if (logger.isDebugEnabled())
			logger.debug("Request finished with {}ms", (System.currentTimeMillis() - start));
	}

	@Override
	public void destroy() {
		ThreadEx.clear();
		actionWrapper.clear();
		actionWrapper = null;
		wrapper = null;
		context.destroy();
		context = null;
	}

}
