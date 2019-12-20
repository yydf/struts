package cn.coder.struts;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import cn.coder.struts.core.RequestDispatcher;
import cn.coder.struts.support.ServletWebRequest;

public class StrutsFilter implements Filter {
	
	private static final String DEFAULT_ENCODING = "UTF-8";
	private RequestDispatcher dispatcher;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		dispatcher = new RequestDispatcher(filterConfig);
		dispatcher.init();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		request.setCharacterEncoding(DEFAULT_ENCODING);
		response.setCharacterEncoding(DEFAULT_ENCODING);
		//分发请求
		dispatcher.doDispatch(new ServletWebRequest(request, response));
	}

	@Override
	public void destroy() {
		dispatcher.clear();
		dispatcher = null;
	}

}
