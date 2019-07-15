package cn.coder.struts;

import java.io.File;
import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.core.StrutsContext;
import cn.coder.struts.support.WebInitializer;
import cn.coder.struts.wrapper.SessionWrapper;

@HandlesTypes(WebInitializer.class)
public final class StrutsContainerInitializer implements ServletContainerInitializer {
	private static final Logger logger = LoggerFactory.getLogger(StrutsContainerInitializer.class);

	private static final String WEB_INF_CLASSES = "/WEB-INF/classes/";
	private static final String SUFFIX_CLASS = ".class";
	private static final String EMPTY = "";

	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		long start = System.currentTimeMillis();
		
		// 扫描classes，获取Context
		StrutsContext context = new StrutsContext(ctx);
		File path = new File(ctx.getRealPath(WEB_INF_CLASSES));
		if (path.exists())
			scanPaths(context, WEB_INF_CLASSES);
		else
			throw new ServletException("Can not found the '" + WEB_INF_CLASSES + "' path");
		ctx.setAttribute("StrutsContext", context);

		// 增加Session监听
		ctx.addListener(SessionWrapper.class);

		// 增加全局过滤器
		ctx.addFilter("StrutsFilter", StrutsFilter.class);

		if (c != null && !c.isEmpty()) {
			try {
				WebInitializer initializer;
				for (Class<?> clazz : c) {
					initializer = (WebInitializer) clazz.newInstance();
					initializer.onStartup(ctx);
				}
			} catch (Exception e) {
				if (logger.isErrorEnabled())
					logger.error("Start up webinitializer faild", e);
			}
		}

		if (logger.isDebugEnabled())
			logger.debug("Struts container started with {}ms", (System.currentTimeMillis() - start));
	}

	private static void scanPaths(StrutsContext context, String parent) {
		Set<String> paths = context.getResourcePaths(parent);
		if (paths != null) {
			for (String path : paths) {
				if (path.endsWith("/"))
					scanPaths(context, path);
				else if (path.endsWith(SUFFIX_CLASS)) {
					path = path.replace(WEB_INF_CLASSES, EMPTY);
					path = path.replace(SUFFIX_CLASS, EMPTY);
					path = path.replace('/', '.');
					context.split(path);
				} else {

				}
			}
		}
	}

}
