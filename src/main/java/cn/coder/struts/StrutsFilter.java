package cn.coder.struts;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import cn.coder.struts.core.URIDispatcher;
import cn.coder.struts.holder.RequestHolder;

public class StrutsFilter implements Filter {

	private static final String DEFAULT_ENCODING = "UTF-8";
	private URIDispatcher dispatcher;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		dispatcher = new URIDispatcher(filterConfig);
		dispatcher.init();
	}

	@Override
	public void doFilter(final ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		request.setCharacterEncoding(DEFAULT_ENCODING);
		response.setCharacterEncoding(DEFAULT_ENCODING);
		
		RequestHolder.hold(request, response);
		
		// 分发请求
		if (!dispatcher.doDispatch()) {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		dispatcher.clear();
		dispatcher = null;
	}

}
