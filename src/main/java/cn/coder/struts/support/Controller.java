package cn.coder.struts.support;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.util.BeanUtils;
import cn.coder.struts.util.DESUtils;
import cn.coder.struts.util.StringUtils;
import cn.coder.struts.view.ModelAndView;

/**
 * 基础控制类
 * 
 * @author YYDF
 *
 */
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
		return getParameter(name, String.class);
	}

	protected void setSessionAttr(String name, Object value) {
		LOCAL_DATA.get().setSessionAttr(name, value);
	}

	protected void removeSessionAttr(String name) {
		LOCAL_DATA.get().removeSessionAttr(name);
	}

	@SuppressWarnings("unchecked")
	public <T> T getParameter(String name, Class<T> type) {
		String str = LOCAL_DATA.get().getParameter(name);
		if (str != null)
			return (T) BeanUtils.valueToType(type, StringUtils.filterJSNull(str));
		return null;
	}

	protected MultipartFile getMultipartFile(String name) {
		return LOCAL_DATA.get().getMultipartFile(name);
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
		return LOCAL_DATA.get().getRemoteAddr();
	}

	protected static ModelAndView getView(String name) {
		return new ModelAndView(name);
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

	public void clear() {
		LOCAL_DATA.remove();
	}
}
