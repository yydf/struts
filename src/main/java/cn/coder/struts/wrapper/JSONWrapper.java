package cn.coder.struts.wrapper;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.util.BeanUtils;

public final class JSONWrapper {
	private static final Logger logger = LoggerFactory.getLogger(JSONWrapper.class);

	private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};

	private static final String COMMA = ",";
	private static final String MARKS = "\"";
	private static final String COLON = ":";
	private static final String BRACKET_LEFT = "[";
	private static final String BRACKET_RIGHT = "]";
	private static final String BRACE_LEFT = "{";
	private static final String BRACE_RIGHT = "}";
	private static final String STR_VERSION_UID = "serialVersionUID";

	private final StringBuilder json = new StringBuilder();

	public String write(Map<String, Object> jsonMap) {
		try {
			appendMap(jsonMap);
		} catch (RuntimeException e) {
			if (logger.isErrorEnabled())
				logger.error("Write json faild", e);
		}
		return json.toString();
	}

	private void appendMap(Map<?, ?> map) {
		json.append(BRACE_LEFT);
		Iterator<?> keys = map.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			appendObj(key, map.get(key), keys.hasNext());
		}
		checkLast(json);
		json.append(BRACE_RIGHT);
	}

	private void appendObj(String key, Object obj, boolean hasNext) {
		if (obj == null || STR_VERSION_UID.equals(key))
			return;
		if (key != null)
			json.append(MARKS).append(key).append(MARKS).append(COLON);
		if (isNumber(obj))
			appendNum(obj);
		else if (isString(obj))
			appendString(obj.toString());
		else if (isDate(obj))
			appendDate(obj);
		else if (isArray(obj))
			appendArray(obj);
		else if (isList(obj))
			appendList((List<?>) obj);
		else if (isMap(obj))
			appendMap((Map<?, ?>) obj);
		else
			appendBean(obj);
		if (hasNext)
			json.append(COMMA);
	}

	private void appendDate(Object obj) {
		json.append(MARKS).append(sdf.get().format(obj)).append(MARKS);
	}

	private void appendBean(Object obj) {
		json.append(BRACE_LEFT);
		Map<String, Object> map = getBeanValue(obj);
		Iterator<String> keys = map.keySet().iterator();
		String key;
		while (keys.hasNext()) {
			key = keys.next();
			appendObj(key, map.get(key), keys.hasNext());
		}
		checkLast(json);
		json.append(BRACE_RIGHT);
	}

	private static Map<String, Object> getBeanValue(Object obj) {
		HashMap<String, Object> map = new HashMap<>();
		try {
			Set<Field> fields = BeanUtils.getDeclaredFields(obj.getClass());
			Object obj2;
			for (Field field : fields) {
				if (!field.isAccessible())
					field.setAccessible(true);
				obj2 = field.get(obj);
				if (obj2 != null)
					map.put(field.getName(), obj2);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			if (logger.isErrorEnabled())
				logger.error("Fetch bean value faild", e);
		}
		return map;
	}

	private void appendList(List<?> list) {
		json.append(BRACKET_LEFT);
		int len = list.size();
		for (int i = 0; i < len; i++) {
			appendObj(null, list.get(i), i != (len - 1));
		}
		checkLast(json);
		json.append(BRACKET_RIGHT);
	}

	private void appendArray(Object obj) {
		json.append(BRACKET_LEFT);
		int len = Array.getLength(obj);
		for (int i = 0; i < len; i++) {
			appendObj(null, Array.get(obj, i), i != (len - 1));
		}
		checkLast(json);
		json.append(BRACKET_RIGHT);
	}

	private void appendNum(Object obj) {
		json.append(obj);
	}

	private void appendString(String str) {
		// 转换\n\r\t
		if (str.indexOf("\n") > -1)
			str = str.replace("\n", "\\n");
		if (str.indexOf("\r") > -1)
			str = str.replace("\r", "\\r");
		if (str.indexOf("\t") > -1)
			str = str.replace("\t", "\\t");
		// 存在单斜杠，则转成双斜杠
		if (str.indexOf("\"") > -1)
			str = str.replace("\"", "\\\"");
		json.append(MARKS).append(str).append(MARKS);
	}

	private static void checkLast(StringBuilder json2) {
		int len = json2.length();
		if (COMMA.equals(json2.substring(len - 1, len)))
			json2.deleteCharAt(len - 1);
	}

	private static boolean isMap(Object obj) {
		return obj instanceof Map;
	}

	private static boolean isList(Object obj) {
		return obj instanceof List;
	}

	private static boolean isArray(Object obj) {
		return obj.getClass().isArray();
	}

	private boolean isDate(Object obj) {
		return obj instanceof Date;
	}

	private static boolean isString(Object obj) {
		return obj instanceof CharSequence || obj instanceof Character;
	}

	private static boolean isNumber(Object obj) {
		return obj instanceof Integer || obj instanceof Boolean || obj instanceof Double || obj instanceof Long
				|| obj instanceof Byte || obj instanceof Float || obj instanceof Short;
	}
}
