package cn.coder.struts.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateEx {

	public static String today(String join) {
		return today(join, false);
	}

	private static String today(String join, boolean withTime) {
		StringBuilder format = new StringBuilder();
		format.append("yyyy").append(join).append("MM").append(join).append("dd");
		if (withTime)
			format.append(" ").append("HH:mm:ss").toString();
		SimpleDateFormat sdf = new SimpleDateFormat(format.toString());
		return sdf.format(new Date());
	}

	public static Date toDate(Object value) {
		if (StringUtils.isEmpty(value))
			return null;
		if (value instanceof Date)
			return (Date) value;
		int len = value.toString().length();
		String format;
		if (len == 8)
			format = (value.toString().indexOf(":") != -1) ? "HH:mm:ss" : "yyyyMMdd";
		else if (len == 10)
			format = "yyyy-MM-dd";
		else if (len == 14)
			format = "yyyyMMddHHmmss";
		else if (len == 19)
			format = "yyyy-MM-dd HH:mm:ss";
		else
			throw new NullPointerException("Unsuppord date length " + len);
		return toDate(value.toString(), format);
	}

	public static Date toDate(String str, String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.parse(str);
		} catch (ParseException e) {
			throw new RuntimeException("Format date '" + str + "' with '" + format + "' faild");
		}
	}
}
