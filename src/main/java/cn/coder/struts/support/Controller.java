package cn.coder.struts.support;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.util.BeanUtils;
import cn.coder.struts.util.StringUtils;
import cn.coder.struts.view.JSONMap;

public abstract class Controller {

	private static final ThreadLocal<ServletWebRequest> LOCAL_DATA = new ThreadLocal<>();

	public void init(ServletWebRequest web) {
		LOCAL_DATA.set(web);
	}

	protected HttpServletRequest getRequest() {
		return LOCAL_DATA.get().getRequest();
	}

	protected HttpServletResponse getResponse() {
		return LOCAL_DATA.get().getResponse();
	}

	protected Object getSession(String attr) {
		return LOCAL_DATA.get().getSession(attr);
	}

	protected String getParameter(String name) {
		return getParameter(String.class, name);
	}

	@SuppressWarnings("unchecked")
	public <T> T getParameter(Class<T> clazz, String name) {
		String str = LOCAL_DATA.get().getParameter(name);
		if (str != null)
			return (T) BeanUtils.valueToType(clazz, StringUtils.filterJSNull(str));
		return null;
	}
	
	protected JSONMap getOK(Object... obj) {
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
	
	protected JSONMap getOK(Map<String, Object> map) {
		return JSONMap.success().putAll(map);
	}

	protected JSONMap getError(int errId, String msg) {
		return JSONMap.error(errId, msg);
	}

	public void clear() {
		LOCAL_DATA.remove();
	}
}
