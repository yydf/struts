package cn.coder.struts.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import cn.coder.struts.annotation.Before;

public final class BeanUtils {
	private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};

	public static void setValue(Field field, Object obj, Object value) {
		if (Modifier.isFinal(field.getModifiers()))
			return;
		try {
			if (!field.isAccessible())
				field.setAccessible(true);
			field.set(obj, toValue(field.getType(), value));
		} catch (Exception e) {
			throw new RuntimeException("Set value faild", e);
		}
	}

	public static Object toValue(Class<?> type, Object value) {
		if (value == null)
			return null;
		if (value.getClass().equals(type))
			return value;
		switch (type.getName()) {
		case "java.lang.String":
			if (value instanceof Date)
				return sdf.get().format(value);
			return value.toString();
		case "int":
		case "java.lang.Integer":
			return ("".equals(value) ? null : Integer.parseInt(value.toString()));
		case "long":
		case "java.lang.Long":
			if (value instanceof Date)
				return ((Date) value).getTime();
			return ("".equals(value) ? null : Long.parseLong(value.toString()));
		case "boolean":
		case "java.lang.Boolean":
			return ("".equals(value) ? null : Boolean.parseBoolean(value.toString()));
		case "double":
		case "java.lang.Double":
			return ("".equals(value) ? null : Double.parseDouble(value.toString()));
		case "float":
		case "java.lang.Float":
			return ("".equals(value) ? null : Float.parseFloat(value.toString()));
		case "java.util.Date":
			return DateEx.toDate(value);
		default:
			throw new RuntimeException("Unkonwn field type " + type.getName());
		}
	}

	public static Class<?>[] mergeInterceptor(Before b1, Before b2) {
		if (b1 == null && b2 == null)
			return new Class<?>[0];
		if (b1 == null || b1.value().length == 0)
			return b2.value();
		if (b2 == null || b2.value().length == 0)
			return b1.value();
		Class<?>[] arr1 = b1.value();
		Class<?>[] arr2 = b2.value();
		Class<?>[] arr = new Class<?>[arr1.length + arr2.length];
		System.arraycopy(arr1, 0, arr, 0, arr1.length);
		System.arraycopy(arr2, 0, arr, arr1.length, arr2.length);
		return arr;
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

}
