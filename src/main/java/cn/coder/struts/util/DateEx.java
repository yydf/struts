package cn.coder.struts.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
	private static final Calendar c = Calendar.getInstance();

	public static String today(String join) {
		return today(join, false);
	}

	public static String today(String join, boolean withTime) {
		return today(join, null, withTime, 0);
	}

	public static String today(String join, TimeUnit unit, int amount) {
		return today(join, unit, false, amount);
	}

	public static String today(String join, TimeUnit unit, boolean withTime, int amount) {
		StringBuilder format = new StringBuilder();
		format.append("yyyy").append(join).append("MM").append(join).append("dd");
		if (withTime)
			format.append(" ").append("HH:mm:ss").toString();
		c.setTime(new Date());
		if (amount > 0 && unit != null) {
			switch (unit) {
			case YEAR:
				c.add(Calendar.YEAR, amount);
				break;
			case MONTH:
				c.add(Calendar.MONTH, amount);
				break;
			case DAY:
				c.add(Calendar.DATE, amount);
				break;
			case HOUR:
				c.add(Calendar.HOUR, amount);
				break;
			case MINUTE:
				c.add(Calendar.MINUTE, amount);
				break;
			case SECOND:
				c.add(Calendar.SECOND, amount);
				break;
			default:
				break;
			}
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format.toString());
		return sdf.format(c.getTime());
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
			if (logger.isErrorEnabled())
				logger.error("Parse date '{}' faild", str, e);
			return null;
		}
	}

	public enum TimeUnit {
		YEAR, MONTH, DAY, HOUR, MINUTE, SECOND
	}

}
