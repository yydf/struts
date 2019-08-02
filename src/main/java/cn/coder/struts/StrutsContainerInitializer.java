package cn.coder.struts;

import java.io.File;
import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.core.StrutsContext;
import cn.coder.struts.util.ContextUtils;
import cn.coder.struts.wrapper.SessionWrapper;

public final class StrutsContainerInitializer implements ServletContainerInitializer {
	private static final Logger logger = LoggerFactory.getLogger(StrutsContainerInitializer.class);

	private static final String WEB_INF_CLASSES = ContextUtils.WEB_INF_CLASSES;

	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		long start = System.currentTimeMillis();

		// 扫描classes，获取Context
		StrutsContext context = new StrutsContext(ctx);
		File path = new File(ctx.getRealPath(WEB_INF_CLASSES));
		if (path.exists()) {
			context.scanPaths(WEB_INF_CLASSES);
			context.sortClass();
		} else
			throw new ServletException("Can not found the '" + WEB_INF_CLASSES + "' path");
		ctx.setAttribute("StrutsContext", context);

		// 增加Session监听
		ctx.addListener(SessionWrapper.class);

		// 增加全局过滤器
		ctx.addFilter("StrutsFilter", StrutsFilter.class);

		if (logger.isDebugEnabled())
			logger.debug("Struts container started with {}ms", (System.currentTimeMillis() - start));
	}

}
