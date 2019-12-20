package cn.coder.struts.core;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.support.ServletWebRequest;

public abstract class AbstractDispatcher {

	protected ApplicationContext context;
	private static final String ATTRIBUTE_APPLICATION_CONTEXT = ApplicationContext.class.getName() + ".CONTEXT";

	public AbstractDispatcher(ServletContext servletContext) {
		createApplicationContext(servletContext);
	}

	private void createApplicationContext(ServletContext servletContext) {
		this.context = new ApplicationContext(servletContext);
		servletContext.setAttribute(ATTRIBUTE_APPLICATION_CONTEXT, this.context);
		this.context.doScan();
	}

	protected static void noHandlerFound(ServletWebRequest req) throws IOException {
		req.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
}
