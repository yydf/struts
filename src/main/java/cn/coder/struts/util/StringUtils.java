package cn.coder.struts.util;

public class StringUtils {

	public static final String STR_NAN = "NaN";
	public static final String STR_UNDEFINED = "undefined";
	public static final String STR_NULL = "null";
	public static final String STR_EMPTY = "";

	public static boolean isEmpty(Object obj) {
		return obj == null || obj.toString().length() == 0;
	}

	public static boolean isNotBlank(Object obj) {
		return isEmpty(obj) == false;
	}

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

	public static String sub(String str, int len, String replace) {
		if (str == null)
			return str;
		if (str.length() >= len)
			return str.substring(0, len) + replace;
		return str;
	}

	/**
	 * 移除JS的无效值
	 * 
	 * @param str
	 *            传入值
	 * @return null或传入值
	 */
	public static String filterJSNull(String str) {
		if (str == null || STR_NULL.equals(str) || STR_UNDEFINED.equals(str) || STR_NAN.equals(str))
			return null;
		return str;
	}
}
