package cn.coder.struts.support;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cn.coder.struts.wrapper.MultipartRequestWrapper;

public final class ServletWebRequest {

	private HttpSession session;
	private HttpServletRequest req;
	private HttpServletResponse res;
	private MultipartRequestWrapper multipartWrapper;

	public ServletWebRequest(ServletRequest request, ServletResponse response) {
		this.req = (HttpServletRequest) request;
		this.res = (HttpServletResponse) response;
		this.session = this.req.getSession();
		if (MultipartRequestWrapper.isMultipartContent(this.req)) {
			multipartWrapper = new MultipartRequestWrapper(this.req);
		}
	}

	public boolean isMultipartRequest() {
		return this.multipartWrapper != null;
	}

	public Object getSession(String attr) {
		return this.session.getAttribute(attr);
	}

	public HttpSession getSession() {
		return this.session;
	}

	public HttpServletRequest getRequest() {
		return this.req;
	}

	public HttpServletResponse getResponse() {
		return this.res;
	}

	public String getParameter(String name) {
		// Attribute优先级高
		Object obj = this.req.getAttribute(name);
		if (obj == null) {
			if (multipartWrapper != null)
				return multipartWrapper.getField(name, null);
			return this.req.getParameter(name);
		} else
			return obj.toString();
	}

	public MultipartFile getMultipartFile(String name) {
		if (multipartWrapper != null)
			return multipartWrapper.getMultipartFile(name);
		return null;
	}

	public Iterator<MultipartFile> getMultipartFiles() {
		if (multipartWrapper != null)
			return multipartWrapper.getMultipartFiles();
		return null;
	}

	public String getMethod() {
		return this.req.getMethod();
	}

	public String getRequestURI() {
		return this.req.getRequestURI();
	}

	public void setContentType(String type) {
		this.res.setContentType(type);
	}

	public String getServletPath() {
		return this.req.getServletPath();
	}

	public void forward(String path) throws ServletException, IOException {
		this.req.getRequestDispatcher(path).forward(this.req, this.res);
	}

	public void setRequestAttr(String name, Object value) {
		this.req.setAttribute(name, value);
	}

	public void setSessionAttr(String name, Object value) {
		this.session.setAttribute(name, value);
	}

	public PrintWriter getWriter() throws IOException {
		return this.res.getWriter();
	}

	public void sendError(int sc) throws IOException {
		this.res.sendError(sc);
	}
	
	public void sendError(int sc, String msg) throws IOException {
		this.res.sendError(sc, msg);
	}

	public void clear() {
		if (this.multipartWrapper != null) {
			this.multipartWrapper.clear();
			this.multipartWrapper = null;
		}
		this.session = null;
		this.req = null;
		this.res = null;
	}

}
