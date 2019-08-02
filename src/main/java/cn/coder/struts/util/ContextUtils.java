package cn.coder.struts.util;

import java.util.Set;

import cn.coder.struts.annotation.Before;
import cn.coder.struts.annotation.Request;
import cn.coder.struts.core.StrutsContext;

public final class ContextUtils {
	
	public static final String WEB_INF_CLASSES = "/WEB-INF/classes/";
	public static final String SUFFIX_CLASS = ".class";
	public static final String EMPTY = "";
	
	public static void scanPaths(StrutsContext context, String parent) {
		Set<String> paths = context.getResourcePaths(parent);
		if (paths != null) {
			for (String path : paths) {
				if (path.endsWith("/"))
					scanPaths(context, path);
				else if (path.endsWith(SUFFIX_CLASS)) {
					path = path.replace(WEB_INF_CLASSES, EMPTY);
					path = path.replace(SUFFIX_CLASS, EMPTY);
					path = path.replace('/', '.');
					context.group(BeanUtils.toClass(path));
				} else {

				}
			}
		}
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
}
