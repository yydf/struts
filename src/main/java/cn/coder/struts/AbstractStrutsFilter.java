package cn.coder.struts;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.event.FileUploadListener;
import cn.coder.struts.event.StrutsEventListener;
import cn.coder.struts.handler.Handler;
import cn.coder.struts.handler.HandlerAdapter;
import cn.coder.struts.handler.HandlerChain;
import cn.coder.struts.mvc.ServletRequestHolder;
import cn.coder.struts.view.View;
import cn.coder.struts.wrapper.MultipartRequestWrapper;

public abstract class AbstractStrutsFilter implements Filter {

	private StrutsApplicationContext context;
	private List<Handler> handlers;
	private List<HandlerAdapter> adapters;
	private List<View> views;
	private List<StrutsEventListener> listeners;
	private FileUploadListener uploadListener;
	private boolean multipartContent = false;

	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final String MULTIPART_ATTRIBUTE = "struts.servlet.multipart.wrapper";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.context = new StrutsApplicationContext(filterConfig);
		this.handlers = this.context.getHandlers();
		this.adapters = this.context.getAdapters();
		this.views = this.context.getViews();
		this.listeners = this.context.getListeners();
		this.uploadListener = this.context.getFileUploadListener();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		long startTime = System.currentTimeMillis();

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		// 设置编码
		req.setCharacterEncoding(DEFAULT_ENCODING);
		res.setCharacterEncoding(DEFAULT_ENCODING);

		ServletRequestHolder.hold(req, res);

		doDispatch(startTime, req, res);
	}

	protected void checkMultipart(HttpServletRequest request) {
		this.multipartContent = MultipartRequestWrapper.isMultipartContent(request);
		if (this.multipartContent) {
			request.setAttribute(MULTIPART_ATTRIBUTE, new MultipartRequestWrapper(request, this.uploadListener));
		}
	}

	protected void clearMultipart(HttpServletRequest request) {
		if (this.multipartContent) {
			((MultipartRequestWrapper) request.getAttribute(MULTIPART_ATTRIBUTE)).clear();
			request.removeAttribute(MULTIPART_ATTRIBUTE);
		}
	}

	protected abstract void doDispatch(long startTime, HttpServletRequest request, HttpServletResponse response)
			throws ServletException;

	protected HandlerChain getHandlerChain(HttpServletRequest req) {
		HandlerChain chain;
		for (Handler handler : this.handlers) {
			chain = handler.getHandlerChain(req);
			if (chain != null)
				return chain;
		}
		return null;
	}

	protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
		for (HandlerAdapter adapter : this.adapters) {
			if (adapter.supports(handler))
				return adapter;
		}
		throw new ServletException("No HandlerAdapter found for handler '" + handler + "'");
	}

	protected View getView(Object result) throws ServletException {
		for (View view : this.views) {
			if (view.supports(result))
				return view;
		}
		throw new ServletException("No View found for result '" + result.getClass().getName() + "'");
	}

	protected void publishEvent(Object event) {
		for (StrutsEventListener listener : this.listeners) {
			if (listener.listen(event.getClass()))
				listener.onEvent(event);
		}
	}

	@Override
	public void destroy() {
		this.multipartContent = false;
		this.handlers = null;
		this.adapters = null;
		this.views = null;
		this.listeners = null;
		if (this.context != null) {
			this.context.clear();
			this.context = null;
		}
	}

}
