package cn.coder.struts.support;

import java.lang.reflect.Field;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.core.ServletRequestHolder;
import cn.coder.struts.util.BeanUtils;
import cn.coder.struts.util.StringUtils;
import cn.coder.struts.view.MultipartFile;
import cn.coder.struts.wrapper.MultipartRequestWrapper;
import cn.coder.struts.wrapper.MultipartRequestWrapper.processFile;
import cn.coder.struts.wrapper.SessionWrapper;

public abstract class ActionSupport implements processFile {
	private static final Logger logger = LoggerFactory.getLogger(ActionSupport.class);

	private static final String MULTIPART_ATTRIBUTE = "struts.servlet.multipart.wrapper";

	protected HttpServletRequest getRequest() {
		return ServletRequestHolder.getRequest();
	}

	protected HttpServletResponse getResponse() {
		return ServletRequestHolder.getResponse();
	}
	
	protected HttpSession getSession() {
		return getRequest().getSession();
	}

	protected Object getSession(String name) {
		return getSession().getAttribute(name);
	}
	
	protected Object getSession(String name, String sessionId) {
		return SessionWrapper.getAttribute(name, sessionId);
	}

	protected Object getAttribute(String name) {
		return getRequest().getAttribute(name);
	}

	protected MultipartRequestWrapper getMultipartRequestWrapper() {
		return (MultipartRequestWrapper) getAttribute(MULTIPART_ATTRIBUTE);
	}

	@SuppressWarnings("unchecked")
	public <T> T getParameter(Class<T> clazz, String name) {
		// 最高优先级
		Object value = getRequest().getAttribute(name);
		String str = value == null ? getRequest().getParameter(name) : value.toString();
		MultipartRequestWrapper multipartWrapper = getMultipartRequestWrapper();
		if (multipartWrapper != null)
			str = multipartWrapper.getField(name, str);
		return (T) BeanUtils.toValue(clazz, StringUtils.filterJSNull(str));
	}

	public String getParameter(String name) {
		return getParameter(String.class, name);
	}

	protected MultipartFile getMultipartFile(String name) {
		MultipartRequestWrapper multipartWrapper = getMultipartRequestWrapper();
		if (multipartWrapper != null)
			return multipartWrapper.getMultipartFile(name);
		return null;
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
		String ip = getRequest().getRemoteAddr();
		if ("127.0.0.1".equals(ip))
			return getRequest().getHeader("X-Real-IP");
		return ip;
	}

	public void clear() {
	}

}
