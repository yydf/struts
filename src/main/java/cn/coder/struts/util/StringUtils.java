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

	/**
	 * 字符串补充
	 * 
	 * @param orgin
	 * @param str
	 * @param len
	 * @return
	 */
	public static String padLeft(Object orgin, String str, int len) {
		if (orgin == null)
			return null;
		int length = orgin.toString().length();
		int remaining = len - length;
		if (remaining > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < remaining; i++) {
				sb.append(str);
			}
			sb.append(orgin);
			return sb.toString();
		}
		return orgin.toString();
	}

}
