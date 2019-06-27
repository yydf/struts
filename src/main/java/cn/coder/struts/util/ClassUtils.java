package cn.coder.struts.util;

import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.annotation.Request;

public class ClassUtils {
	private static final Logger logger = LoggerFactory.getLogger(ClassUtils.class);

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
			if (logger.isErrorEnabled())
				logger.error("Class not fund '{}'", path, e);
			return null;
		}
	}

	public static boolean isSupportGZip(HttpServletRequest req) {
		String encoding = req.getHeader("Accept-Encoding");
		return encoding != null && encoding.indexOf("gzip") > -1;
	}

	public static String getUrlMapping(Request classReq, String path) {
		// 如果以~开始，则视为根目录
		if (path.startsWith("~"))
			return path.substring(1);
		path = path.startsWith("/") ? path : (path + "/");
		if (classReq != null)
			return classReq.value() + path;
		return path;

	}

	public interface FilterClassType {

		void filter(Class<?> class1);

	}
}
