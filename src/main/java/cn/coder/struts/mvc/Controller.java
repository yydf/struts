package cn.coder.struts.mvc;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cn.coder.struts.util.BeanUtils;
import cn.coder.struts.util.DESUtils;
import cn.coder.struts.util.StringUtils;
import cn.coder.struts.view.JSPView;
import cn.coder.struts.wrapper.MultipartRequestWrapper;

/**
 * 基础控制类
 * 
 * @author YYDF
 *
 */
public abstract class Controller {
	
	protected HttpServletRequest getRequest() {
		return ServletRequestHolder.getRequestContext();
	}

	protected HttpServletResponse getResponse() {
		return ServletRequestHolder.getResponseContext();
	}

	protected HttpSession getSession() {
		return getRequest().getSession();
	}

	protected Object getSession(String attr) {
		return getSession().getAttribute(attr);
	}

	protected Object getAttribute(String name) {
		return getRequest().getAttribute(name);
	}

	protected String getParameter(String name) {
		return getParameter(name, String.class);
	}

	protected void setSessionAttr(String name, Object value) {
		getSession().setAttribute(name, value);
	}

	protected void removeSessionAttr(String name) {
		getSession().removeAttribute(name);
	}

	@SuppressWarnings("unchecked")
	public <T> T getParameter(String name, Class<T> type) {
		// Attribute优先级高
		Object obj = getRequest().getAttribute(name);
		if (obj == null) {
			MultipartRequestWrapper wrapper = (MultipartRequestWrapper) getAttribute(
					"struts.servlet.multipart.wrapper");
			if (wrapper != null)
				obj = wrapper.getField(name, null);
			else
				obj = getRequest().getParameter(name);
		}
		if (obj != null)
			return (T) BeanUtils.valueToType(type, StringUtils.filterJSNull(obj));
		return null;
	}

	protected MultipartFile getMultipartFile(String name) {
		MultipartRequestWrapper wrapper = (MultipartRequestWrapper) getAttribute("struts.servlet.multipart.wrapper");
		if (wrapper != null)
			return wrapper.getMultipartFile(name);
		return null;
	}

	public <T> T getPostData(Class<T> type) {
		try {
			T entity = type.newInstance();
			Object obj;
			Field[] fields = BeanUtils.getDeclaredFields(type);
			for (Field field : fields) {
				obj = getParameter(field.getName(), field.getType());
				if (obj != null) {
					BeanUtils.setValue(field, entity, obj);
				}
			}
			return entity;
		} catch (Exception e) {
			throw new RuntimeException("getPostData faild", e);
		}
	}

	protected String getRemoteAddr() {
		return getRequest().getRemoteAddr();
	}

	protected static JSPView getView(String name) {
		return new JSPView(name);
	}

	protected static String createToken(String key, Object... args) throws Exception {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			str.append(args[i]).append(",");
		}
		str.append(System.currentTimeMillis());
		String token = DESUtils.encrypt(str.toString(), key);
		return URLEncoder.encode(token, "utf-8");
	}

	protected static JSONMap getOK(Object... obj) {
		JSONMap jsonMap = JSONMap.success();
		String key = "";
		for (int i = 0; i < obj.length; i++) {
			if (i % 2 == 0) {
				key = (String) obj[i];
			} else {
				jsonMap.put(key, obj[i]);
			}
		}
		return jsonMap;
	}

	protected static JSONMap getOK(Map<String, Object> map) {
		return JSONMap.success().putAll(map);
	}

	protected static JSONMap getError(int errId, String msg) {
		return JSONMap.error(errId, msg);
	}
}
