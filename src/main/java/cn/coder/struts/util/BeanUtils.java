package cn.coder.struts.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.annotation.Request;

public class BeanUtils {
	private static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);

	public static String toBeanName(String clazz) {
		clazz = clazz.replace("/WEB-INF/classes/", "");
		clazz = clazz.replace('/', '.');
		clazz = clazz.replace(".class", "");
		return clazz;
	}

	public static Class<?> toClass(String clazz) {
		try {
			return Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			if (logger.isWarnEnabled())
				logger.warn("'{}' not a class", clazz);
		}
		return null;
	}

	public static String genericPath(Request req, Request req2) {
		String path = req2.value().startsWith("/") ? req2.value() : "/" + req2.value();
		if (path.startsWith("~"))
			return path.substring(1);
		if (req == null)
			return path;
		String base = req.value().startsWith("/") ? req.value() : "/" + req.value();
		return base + path;
	}

	public static Field[] getDeclaredFields(Class<?> clazz) {
		HashSet<Field> fieldList = new HashSet<>();
		getDeclaredFields(clazz, fieldList);
		Field[] temp = new Field[fieldList.size()];
		return fieldList.toArray(temp);
	}

	private static void getDeclaredFields(Class<?> clazz, HashSet<Field> fieldList) {
		if (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				fieldList.add(field);
			}
			getDeclaredFields(clazz.getSuperclass(), fieldList);
		}
	}

	// private static final List<String> PRIMITIVE_TYPES =
	// Arrays.asList("boolean", "byte", "char", "short", "int", "long",
	// "float", "double");

	public static Object valueToType(Class<?> type, String value) {
		// 判断空或null，直接返回
		if (value == null)
			return null;
		// 不为空
		if (value.trim().length() > 0) {
			switch (type.getName()) {
			case "int":
			case "java.lang.Integer":
				return Integer.parseInt(value);
			case "long":
			case "java.lang.Long":
				return Long.parseLong(value);
			case "java.util.Date":
				return DateEx.toDate(value);
			default:
				return value;
			}
		}
		// 如果类型是字符串
		if ("java.lang.String".equals(type.getName()))
			return value.trim();
		return null;
	}

	public static void setValue(Field field, Object obj, Object value) throws Exception {
		if (Modifier.isFinal(field.getModifiers()))
			return;
		if (!field.isAccessible())
			field.setAccessible(true);
		field.set(obj, value);
	}

}
