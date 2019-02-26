package cn.coder.struts;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.support.WebInitializer;
import cn.coder.struts.wrapper.SessionWrapper;

@HandlesTypes(WebInitializer.class)
public class StrutsContainerInitializer implements ServletContainerInitializer {
	private static final Logger logger = LoggerFactory.getLogger(StrutsContainerInitializer.class);

	public void onStartup(Set<Class<?>> initializerClasses, ServletContext ctx) throws ServletException {
		ctx.addFilter("StrutsFilter", StrutsFilter.class);
		ctx.addListener(SessionWrapper.class);
		logger.debug("ServletContext start up with a struts filter");
		if (initializerClasses == null || initializerClasses.isEmpty()) {
			logger.debug("No web initializer classes");
			return;
		}

		for (Class<?> clazz : initializerClasses) {
			try {
				((WebInitializer) clazz.newInstance()).onStartup(ctx);
				logger.debug("WebInitializer {} started", clazz.getName());
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error("WebInitializer start faild", e);
			}
		}
	}

}
