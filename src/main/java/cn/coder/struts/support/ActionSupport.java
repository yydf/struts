package cn.coder.struts.support;

import java.lang.reflect.Field;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.util.FieldUtils;
import cn.coder.struts.view.MultipartFile;
import cn.coder.struts.view.Validated;
import cn.coder.struts.wrapper.MultipartRequestWrapper;
import cn.coder.struts.wrapper.MultipartRequestWrapper.processFile;
import cn.coder.struts.wrapper.SessionWrapper;

public abstract class ActionSupport implements processFile {

	static final Logger logger = LoggerFactory.getLogger(ActionSupport.class);
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected boolean isMultipartRequest;
	private MultipartRequestWrapper multipartWrapper;

	public void setRequest(HttpServletRequest req2) {
		this.request = req2;
		this.isMultipartRequest = ServletFileUpload.isMultipartContent(request);
		if (isMultipartRequest) {
			multipartWrapper = new MultipartRequestWrapper(request);
			multipartWrapper.processRequest(this);
			logger.debug("Process multipart request");
		}
	}

	public void setResponse(HttpServletResponse res2) {
		this.response = res2;
	}

	protected HttpServletRequest getRequest() {
		return this.request;
	}

	protected HttpServletResponse getResponse() {
		return this.response;
	}

	protected String getParameter(String name) {
		if (isMultipartRequest)
			return multipartWrapper.getField(name);
		String str = request.getParameter(name);
		if ("null".equals(str) || "undefined".equals(str) || "NaN".equals(str))
			return null;
		return request.getParameter(name);
	}

	protected Object getSession(String name) {
		return request.getSession().getAttribute(name);
	}

	protected void setSession(String name, Object value) {
		request.getSession().setAttribute(name, value);
	}

	protected static Object getSession(String name, String sId) {
		return SessionWrapper.getAttribute(name, sId);
	}

	protected String getSessionId() {
		return request.getSession().getId();
	}

	protected MultipartFile getMultipartFile(String name) {
		if (isMultipartRequest)
			return multipartWrapper.getMultipartFile(name);
		return null;
	}

	protected <T> Validated getPostData(Class<T> clazz) {
		Validated valid = new Validated();
		try {
			T obj = clazz.newInstance();
			Field[] fields = clazz.getDeclaredFields();
			String str;
			for (Field field : fields) {
				str = getParameter(field.getName());
				if (str != null) {
					FieldUtils.setValue(field, obj, str);
				}
			}
			valid.setData(obj);
		} catch (InstantiationException | IllegalAccessException | SQLException e) {
			logger.error("getPostData faild", e);
		}
		return valid;
	}
	
	protected String getRemoteAddr() {
		String ip = request.getRemoteAddr();
		if ("127.0.0.1".equals(ip))
			return request.getHeader("X-Real-IP");
		return ip;
	}

	@Override
	public abstract String processMultipartFile(MultipartFile file);

	public void clear() {
		this.request = null;
		this.response = null;
		this.isMultipartRequest = false;
		if (multipartWrapper != null) {
			multipartWrapper.clear();
			multipartWrapper = null;
		}
	}
}
