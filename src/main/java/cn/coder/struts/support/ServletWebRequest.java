package cn.coder.struts.support;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.wrapper.MultipartRequestWrapper;
import cn.coder.struts.wrapper.MultipartRequestWrapper.processFile;

public final class ServletWebRequest {
	private static final Logger logger = LoggerFactory.getLogger(ServletWebRequest.class);

	private HttpSession session;
	private HttpServletRequest req;
	private HttpServletResponse res;
	private MultipartRequestWrapper multipartWrapper;
	private boolean multipartContent;

	public ServletWebRequest(ServletRequest request, ServletResponse response) {
		this.req = (HttpServletRequest) request;
		this.res = (HttpServletResponse) response;
		this.session = this.req.getSession();
		this.multipartContent = MultipartRequestWrapper.isMultipartContent(this.req);
		if (this.multipartContent && logger.isDebugEnabled())
			logger.debug("Multipart request");
	}

	public boolean isMultipartContent() {
		return this.multipartContent;
	}

	public void setMultipartRequestProcess(processFile process) {
		this.multipartWrapper = new MultipartRequestWrapper(this.req, process);
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

	public Enumeration<String> getParameterNames() {
		return this.req.getParameterNames();
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

	public String getFullRequestURL() {
		return this.req.getRequestURL().append(this.req.getQueryString()).toString();
	}

	public void setContentType(String type) {
		this.res.setContentType(type);
	}

	public String getServletPath() {
		return this.req.getServletPath();
	}

	public String getRemoteAddr() {
		return this.req.getRemoteAddr();
	}

	public boolean supportGzip() {
		String accept = req.getHeader("Accept-Encoding");
		return accept != null && accept.indexOf("gzip") > -1;
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

	public void removeSessionAttr(String name) {
		this.session.removeAttribute(name);
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
