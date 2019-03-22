package cn.coder.struts.util;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Set;

public class ObjectUtils {

	public static Object[] mergeArray(Object[] array, Object... objs) {
		if (array.length == 0 && objs.length == 0)
			return new Object[0];
		if (array.length == 0)
			return objs;
		if (objs.length == 0)
			return array;
		Object[] temp = new Object[array.length + objs.length];
		System.arraycopy(array, 0, temp, 0, array.length);
		System.arraycopy(objs, 0, temp, array.length, objs.length);
		return temp;
	}

	public static Integer toInteger(Object value) {
		if ("".equals(value))
			return null;
		return Integer.parseInt(value.toString());
	}

	public static Long toLong(Object value) {
		if ("".equals(value))
			return null;
		return Long.parseLong(value.toString());
	}

	public static Boolean toBoolean(Object value) {
		if("".equals(value))
			return null;
		return Boolean.parseBoolean(value.toString());
	}

	public static <T> T copyBean(Class<T> clazz, Object obj) throws SQLException {
		try {
			T t = clazz.newInstance();
			Set<Field> fields = FieldUtils.getDeclaredFields(clazz);
			Set<Field> fields2 = FieldUtils.getDeclaredFields(obj.getClass());
			for (Field field : fields) {
				for (Field field2 : fields2) {
					if (field.getName().equals(field2.getName())) {
						if (!field2.isAccessible())
							field2.setAccessible(true);
						FieldUtils.setValue(field, t, field2.get(obj));
					}
				}
			}
			return t;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SQLException e) {
			throw new SQLException("Copy bean faild", e);
		}
	}
}
