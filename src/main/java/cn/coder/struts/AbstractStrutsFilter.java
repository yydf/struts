package cn.coder.struts;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.event.FileUploadListener;
import cn.coder.struts.event.StrutsEventListener;
import cn.coder.struts.handler.Handler;
import cn.coder.struts.handler.HandlerAdapter;
import cn.coder.struts.handler.SimpleExecutor;
import cn.coder.struts.mvc.ServletRequestHolder;
import cn.coder.struts.view.View;
import cn.coder.struts.wrapper.MultipartRequestWrapper;

public abstract class AbstractStrutsFilter {

	private StrutsApplicationContext context;
	private List<Handler> handlers;
	private List<HandlerAdapter> adapters;
	private List<View> views;
	private List<StrutsEventListener> listeners;
	private FileUploadListener uploadListener;
	private boolean multipartContent = false;

	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final String MULTIPART_ATTRIBUTE = "struts.servlet.multipart.wrapper";

	protected void initContext(FilterConfig filterConfig) {
		this.context = new StrutsApplicationContext(filterConfig);
		this.handlers = this.context.getHandlers();
		this.adapters = this.context.getAdapters();
		this.views = this.context.getViews();
		this.listeners = this.context.getListeners();
		this.uploadListener = this.context.getFileUploadListener();
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

	protected void dispatch(long startTime, HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		// 设置编码
		req.setCharacterEncoding(DEFAULT_ENCODING);
		res.setCharacterEncoding(DEFAULT_ENCODING);

		ServletRequestHolder.hold(req, res);

		doDispatch(startTime, req, res);
	}

	protected abstract void doDispatch(long startTime, HttpServletRequest request, HttpServletResponse response)
			throws ServletException;

	protected SimpleExecutor getExecutor(HttpServletRequest req) {
		SimpleExecutor executor;
		for (Handler handler : this.handlers) {
			executor = handler.getExecutor(req);
			if (executor != null)
				return executor;
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

	protected void clear() {
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
