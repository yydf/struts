package cn.coder.struts.support;

import java.lang.reflect.Field;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.util.BeanUtils;
import cn.coder.struts.util.StringUtils;
import cn.coder.struts.view.MultipartFile;
import cn.coder.struts.wrapper.MultipartRequestWrapper;
import cn.coder.struts.wrapper.MultipartRequestWrapper.processFile;
import cn.coder.struts.wrapper.SessionWrapper;

public abstract class ActionSupport implements processFile {
	private static final Logger logger = LoggerFactory.getLogger(ActionSupport.class);

	private HttpSession session;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private boolean isMultipartRequest = false;
	private MultipartRequestWrapper multipartWrapper;

	public void init(HttpServletRequest req, HttpServletResponse res) {
		this.request = req;
		this.response = res;
		this.session = req.getSession();
		this.isMultipartRequest = ServletFileUpload.isMultipartContent(request);
		if (isMultipartRequest) {
			multipartWrapper = new MultipartRequestWrapper(request, this);
			if (logger.isDebugEnabled())
				logger.debug("Process multipart request");
		}
	}

	public abstract String processMultipartFile(MultipartFile file);

	protected HttpSession getSession() {
		return this.session;
	}

	protected HttpServletRequest getRequest() {
		return this.request;
	}

	protected HttpServletResponse getResponse() {
		return this.response;
	}

	protected Object getSession(String name) {
		return this.session.getAttribute(name);
	}

	protected Object getSession(String name, String sessionId) {
		return SessionWrapper.getAttribute(name, sessionId);
	}

	@SuppressWarnings("unchecked")
	public <T> T getParameter(Class<T> clazz, String name) {
		// 最高优先级
		Object value = request.getAttribute(name);
		String str = value == null ? request.getParameter(name) : value.toString();
		if (isMultipartRequest)
			str = multipartWrapper.getField(name, str);
		return (T) BeanUtils.toValue(clazz, StringUtils.filterJSNull(str));
	}

	public String getParameter(String name) {
		return getParameter(String.class, name);
	}

	protected <T> T getPostData(Class<T> clazz) {
		try {
			T obj = clazz.newInstance();
			Set<Field> fields = BeanUtils.getDeclaredFields(clazz);
			String str;
			for (Field field : fields) {
				str = getParameter(field.getName());
				if (str != null) {
					BeanUtils.setValue(field, obj, str);
				}
			}
			return obj;
		} catch (InstantiationException | IllegalAccessException e) {
			if (logger.isErrorEnabled())
				logger.error("getPostData faild", e);
			return null;
		}
	}

	protected String getRemoteAddr() {
		String ip = request.getRemoteAddr();
		if ("127.0.0.1".equals(ip))
			return request.getHeader("X-Real-IP");
		return ip;
	}

	public void clear() {
		this.request = null;
		this.response = null;
		this.session = null;
		this.isMultipartRequest = false;
		if (multipartWrapper != null) {
			multipartWrapper.clear();
			multipartWrapper = null;
		}
	}

}
