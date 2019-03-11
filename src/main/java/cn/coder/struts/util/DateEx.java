package cn.coder.struts.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
//import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日期工具类
 * 
 * @author YYDF 2019-03-07
 */
public class DateEx {
	private static final Logger logger = LoggerFactory.getLogger(DateEx.class);
	// private static final Calendar c = Calendar.getInstance();

	public static String today(String join) {
		return today(join, false);
	}

	public static String today(String join, boolean withTime) {
		StringBuilder format = new StringBuilder();
		format.append("yyyy").append(join).append("MM").append(join).append("dd");
		if (withTime)
			format.append(" ").append("HH:mm:ss").toString();
		SimpleDateFormat sdf = new SimpleDateFormat(format.toString());
		return sdf.format(new Date());
	}

	public static Date toDate(Object value) {
		if (value == null)
			return null;
		if (value instanceof Timestamp)
			return (Date) value;
		int len = value.toString().length();
		String format;
		if (len == 8)
			format = "yyyyMMdd";
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
			logger.error("Parse date faild", e);
			return null;
		}
	}

}
