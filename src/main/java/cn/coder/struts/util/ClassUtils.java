package cn.coder.struts.util;

import java.lang.reflect.Method;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import cn.coder.struts.annotation.Request;
import cn.coder.struts.annotation.Request.HttpMethod;
import cn.coder.struts.support.ActionIntercepter;
import cn.coder.struts.support.ActionSupport;

public class ClassUtils {
	public static void scanClasses(ServletContext ctx, String parent, FilterClassType work) {
		Set<String> paths = ctx.getResourcePaths(parent);
		for (String path : paths) {
			if (path.endsWith("/"))
				scanClasses(ctx, path, work);
			else if (path.endsWith(".class"))
				work.filter(toClass(path));
			else {
				// Nothing
			}
		}
	}

	private static Class<?> toClass(String path) {
		path = path.replace("/WEB-INF/classes/", "");
		path = path.replace(".class", "");
		path = path.replace('/', '.');
		try {
			return Class.forName(path);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public static boolean isController(Class<?> clazz) {
		return ActionSupport.class.isAssignableFrom(clazz);
	}

	public static String getUrlMapping(Request classReq, String path) {
		if (path.startsWith("~"))
			return path.substring(1);
		if (classReq != null)
			return classReq.value() + path;
		return path;
	}

	public static boolean isSupportGZip(HttpServletRequest req) {
		String encoding = req.getHeader("Accept-Encoding");
		return encoding != null && encoding.indexOf("gzip") > -1;
	}

	public static boolean allowHttpMethod(Method method, String httpMethod) {
		Request req = method.getAnnotation(Request.class);
		if (req == null)
			return true;
		if (req.method() == HttpMethod.ALL)
			return true;
		return req.method().name().equals(httpMethod);
	}

	public static boolean isFilter(Class<?> clazz) {
		return ActionIntercepter.class.isAssignableFrom(clazz);
	}

	public interface FilterClassType {

		void filter(Class<?> class1);

	}
}
