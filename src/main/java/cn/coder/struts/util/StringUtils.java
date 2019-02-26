package cn.coder.struts.util;

public class StringUtils {

	public static boolean isEmpty(Object obj) {
		return obj == null || "".equals(obj.toString());
	}

	public static boolean isNotBlank(String field) {
		return !isEmpty(field);
	}

}
