package cn.coder.struts;

import java.util.Set;

import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public final class StrutsContainerInitializer implements ServletContainerInitializer {

	private static final String FILTER_NAME = StrutsFilter.class.getName() + ".FILTER";

	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		// 添加全局的过滤类
		Dynamic dynamic = ctx.addFilter(FILTER_NAME, StrutsFilter.class);
		dynamic.addMappingForUrlPatterns(null, false, "/*");
	}

}
