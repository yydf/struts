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


	/**
	 * 按某个内容补充字符串
	 * 
	 * @param orgin 原始内容
	 * @param c 要补充的字节
	 * @param len 总长度
	 * @return 补充后的字符串
	 */
	public static String padLeft(Object orgin, char c, int len) {
		if (orgin == null)
			return null;
		String orginStr = orgin.toString();
		int length = orginStr.length();
		int remaining = len - length;
		if (remaining > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < remaining; i++) {
				sb.append(c);
			}
			sb.append(orginStr);
			return sb.toString();
		}
		return orginStr;
	}
}
