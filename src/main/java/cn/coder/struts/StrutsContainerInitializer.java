package cn.coder.struts;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import cn.coder.struts.core.StrutsContext;
import cn.coder.struts.wrapper.SessionWrapper;

public final class StrutsContainerInitializer implements ServletContainerInitializer {

	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		// 扫描classes，获取Context
		StrutsContext context = new StrutsContext(ctx);
		scanPaths(context, "/WEB-INF/classes/");
		ctx.setAttribute("StrutsContext", context);

		// 增加Session监听
		ctx.addListener(SessionWrapper.class);

		// 增加全局过滤器
		ctx.addFilter("StrutsFilter", StrutsFilter.class);
	}

	private static void scanPaths(StrutsContext context, String parent) {
		Set<String> paths = context.getResourcePaths(parent);
		if (paths != null) {
			for (String path : paths) {
				if (path.endsWith("/"))
					scanPaths(context, path);
				else if (path.endsWith(".class")) {
					path = path.replace("/WEB-INF/classes/", "");
					path = path.replace(".class", "");
					path = path.replace('/', '.');
					context.split(path);
				} else {

				}
			}
		}
	}

}
