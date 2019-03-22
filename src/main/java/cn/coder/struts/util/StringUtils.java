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

}
