package cn.coder.struts.util;

public class StringUtils {
	private static final String STR_NAN = "NaN";
	private static final String STR_UNDEFINED = "undefined";
	private static final String STR_NULL = "null";

	public static boolean isEmpty(Object obj) {
		return obj == null || obj.toString().length() == 0;
	}

	public static String filterJSNull(Object obj) {
		if (obj == null || STR_NULL.equals(obj) || STR_UNDEFINED.equals(obj) || STR_NAN.equals(obj))
			return null;
		return obj.toString();
	}

}
