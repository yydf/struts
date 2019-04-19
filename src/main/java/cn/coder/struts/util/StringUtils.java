package cn.coder.struts.util;

public class StringUtils {

	public static boolean isEmpty(Object obj) {
		return obj == null || "".equals(obj.toString());
	}

	public static boolean isNotBlank(String field) {
		return !isEmpty(field);
	}

	public static String padLeft(Object orgin, String str, int len) {
		Assert.notNull(orgin, "orgin string");
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
		if (str == null || "null".equals(str) || "undefined".equals(str) || "NaN".equals(str))
			return null;
		return str;
	}
}
