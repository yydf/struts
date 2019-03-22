package cn.coder.struts.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldUtils {
	private static final List<String> PRIMITIVE_TYPE = Arrays.asList("java.lang.String", "java.lang.Integer",
			"java.lang.Long", "java.lang.Boolean");
	private static final Pattern linePattern = Pattern.compile("_(\\w)");
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final HashMap<String, String> convertedNames = new HashMap<>(1024);

	public static void setValue(Field field, Object obj, Object value) throws SQLException {
		if (Modifier.isFinal(field.getModifiers()))
			return;
		try {
			if (!field.isAccessible())
				field.setAccessible(true);
			field.set(obj, toValue(field.getType(), value));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new SQLException("Set value faild", e);
		}
	}

	public static Object toValue(Class<?> type, Object value) throws SQLException {
		if (value == null)
			return null;
		if (value.getClass().equals(type))
			return value;
		switch (type.getName()) {
		case "java.lang.String":
			if (value instanceof Timestamp)
				return sdf.format(value);
			return value.toString();
		case "int":
		case "java.lang.Integer":
			return ObjectUtils.toInteger(value);
		case "long":
		case "java.lang.Long":
			return ObjectUtils.toLong(value);
		case "boolean":
		case "java.lang.Boolean":
			return ObjectUtils.toBoolean(value);
		case "java.util.Date":
			return DateEx.toDate(value);
		default:
			throw new SQLException("Unkonwn field type " + type.getName());
		}
	}

	public static Set<Field> getDeclaredFields(Class<?> clazz) {
		Set<Field> fieldList = new HashSet<>();
		getDeclaredFields(clazz, fieldList);
		return fieldList;
	}

	private static void getDeclaredFields(Class<?> clazz, Set<Field> fieldList) {
		if (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				fieldList.add(field);
			}
			getDeclaredFields(clazz.getSuperclass(), fieldList);
		}
	}

	public static boolean isPrimitive(Class<?> target) {
		if (target.isPrimitive())
			return true;
		if (PRIMITIVE_TYPE.contains(target.getName()))
			return true;
		return false;
	}

	public static String convert(String str) {
		String name;
		if (convertedNames.containsKey(str))
			name = convertedNames.get(str);
		else {
			name = str.toLowerCase();
			Matcher matcher = linePattern.matcher(name);
			StringBuffer sb = new StringBuffer();
			while (matcher.find()) {
				matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
			}
			matcher.appendTail(sb);
			name = sb.toString();
			convertedNames.put(str, name);
		}
		return name;
	}

}
