package cn.coder.struts.support;

import java.lang.reflect.Field;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.util.BeanUtils;
import cn.coder.struts.util.StringUtils;
import cn.coder.struts.view.MultipartFile;
import cn.coder.struts.wrapper.MultipartRequestWrapper;
import cn.coder.struts.wrapper.MultipartRequestWrapper.processFile;
import cn.coder.struts.wrapper.SessionWrapper;

/**
 * Action的基础类<br>
 * 包含request和response<br>
 * 可通过getParameter(name)获取参数值<br>
 * 可通过getMultipartFile获取上传文件对象MultipartFile
 * 
 * @author YYDF
 *
 */
public abstract class ActionSupport implements processFile {

	private static final Logger logger = LoggerFactory.getLogger(ActionSupport.class);
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected boolean isMultipartRequest;
	private MultipartRequestWrapper multipartWrapper;

	public void init(HttpServletRequest req2, HttpServletResponse res2) {
		this.request = req2;
		this.response = res2;
		this.isMultipartRequest = ServletFileUpload.isMultipartContent(request);
		if (isMultipartRequest) {
			multipartWrapper = new MultipartRequestWrapper(request);
			multipartWrapper.processRequest(this);
			if (logger.isDebugEnabled())
				logger.debug("Process multipart request");
		}
	}

	protected HttpServletRequest getRequest() {
		return this.request;
	}

	protected HttpServletResponse getResponse() {
		return this.response;
	}

	protected String getParameter(String name) {
		// 最高优先级
		Object value = request.getAttribute(name);
		String str = value == null ? request.getParameter(name) : value.toString();
		if (isMultipartRequest)
			str = multipartWrapper.getField(name, str);
		return StringUtils.filterJSNull(str);
	}

	protected Object getSession(String name) {
		return request.getSession().getAttribute(name);
	}

	protected Object getSession(String name, String sId) {
		return SessionWrapper.getAttribute(name, sId);
	}

	protected void setSession(String name, Object value) {
		request.getSession().setAttribute(name, value);
	}

	protected String getSessionId() {
		return request.getSession().getId();
	}

	protected MultipartFile getMultipartFile(String name) {
		if (isMultipartRequest)
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
