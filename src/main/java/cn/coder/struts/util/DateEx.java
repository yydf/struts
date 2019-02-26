package cn.coder.struts.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
//import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateEx {
	private static final Logger logger = LoggerFactory.getLogger(DateEx.class);
	// private static final Calendar c = Calendar.getInstance();
	
	public static String today() {
		return today("", false);
	}
	
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
		if (value instanceof String) {
			int len = value.toString().length();
			String format;
			switch (len) {
			case 8:
				format = "yyyyMMdd";
				break;
			case 10:
				format = "yyyy-MM-dd";
				break;
			default:
				format = "yyyy-MM-dd HH:mm:ss";
				break;
			}
			return toDate(value.toString(), format);
		}
		return null;
	}

	private static Date toDate(String str, String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.parse(str);
		} catch (ParseException e) {
			logger.error("Parse date faild", e);
			return null;
		}
	}

}
