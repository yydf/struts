package cn.coder.struts.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JSPView extends AbstractView {
	private static final Logger logger = LoggerFactory.getLogger(JSPView.class);

	private String viewName;
	private final Map<String, Object> data;

	public JSPView() {
		this(null);
	}

	public JSPView(String name) {
		this.viewName = name;
		this.data = new HashMap<>();
	}

	public String getViewName() {
		if (!this.viewName.startsWith("/"))
			this.viewName = "/" + this.viewName;
		return this.viewName;
	}

	public JSPView addObject(String name, Object obj) {
		this.data.put(name, obj);
		return this;
	}

	public void setViewName(String view) {
		this.viewName = view;
	}

	public Map<String, Object> getData() {
		return this.data;
	}

	@Override
	public boolean supports(Object result) {
		return (result instanceof JSPView);
	}

	@Override
	public void render(Object result, HttpServletRequest req, HttpServletResponse res) throws Exception {
		JSPView mav = (JSPView) result;
		Map<String, Object> data = mav.getData();
		for (Entry<String, Object> entry : data.entrySet()) {
			req.setAttribute(entry.getKey(), entry.getValue());
		}
		req.getRequestDispatcher(mav.getViewName()).forward(req, res);
		if (logger.isDebugEnabled()) {
			logger.debug("[RENDER]{}", mav.getViewName());
		}
	}

}
