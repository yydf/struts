package cn.coder.struts.util;

public final class StringUtils {
	public static final String STR_NAN = "NaN";
	public static final String STR_UNDEFINED = "undefined";
	public static final String STR_NULL = "null";
	public static final String STR_EMPTY = "";

	public static boolean isEmpty(Object o) {
		return o == null || o.toString().length() == 0;
	}

	public static String filterJSNull(Object str) {
		if (str == null || STR_NULL.equals(str) || STR_UNDEFINED.equals(str) || STR_NAN.equals(str))
			return null;
		return str.toString();
	}

}
