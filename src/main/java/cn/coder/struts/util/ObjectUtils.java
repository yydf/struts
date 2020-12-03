package cn.coder.struts.util;

public class ObjectUtils {

	public static Object[] merge(Object[] o1, Object[] o2) {
		Object[] arr = new Object[o1.length + o2.length];
		System.arraycopy(o1, 0, arr, 0, o1.length);
		System.arraycopy(o2, 0, arr, o1.length, o2.length);
		return arr;
	}
}
