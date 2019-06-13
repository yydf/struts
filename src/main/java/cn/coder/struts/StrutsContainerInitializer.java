package cn.coder.struts;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.util.ClassUtils;
import cn.coder.struts.util.ClassUtils.FilterClassType;
import cn.coder.struts.wrapper.ActionWrapper;
import cn.coder.struts.wrapper.SessionWrapper;

/**
 * Struts项目配合tomcat启动类<br>
 * 扫描WEB-INF目录下的所有class文件<br>
 * 归类所有的class对象
 * 
 * @author YYDF 2019-05-21
 *
 */
public final class StrutsContainerInitializer implements ServletContainerInitializer, FilterClassType {
	private static final Logger logger = LoggerFactory.getLogger(StrutsContainerInitializer.class);
	private static final String URL_PATTERN = "/*";
	private final ActionWrapper actionWrapper = new ActionWrapper();
	private final ArrayList<Class<?>> classes = new ArrayList<>();
	private final ArrayList<Class<?>> initializers = new ArrayList<>();
	private final ArrayList<Class<?>> filters = new ArrayList<>();

	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		long start = System.currentTimeMillis();
		ClassUtils.scanClasses(ctx, "/", this);
		ctx.setAttribute("ActionWrapper", actionWrapper);
		ctx.setAttribute("Classes", classes);
		ctx.setAttribute("Filters", filters);
		ctx.setAttribute("InitClasses", initializers);

		// 增加session处理类
		ctx.addListener(SessionWrapper.class);

		// 增加全局Filter
		EnumSet<DispatcherType> dispatcherTypes = EnumSet.allOf(DispatcherType.class);
		dispatcherTypes.add(DispatcherType.REQUEST);
		dispatcherTypes.add(DispatcherType.FORWARD);
		ctx.addFilter("StrutsFilter", StrutsFilter.class).addMappingForUrlPatterns(dispatcherTypes, true, URL_PATTERN);

		if (logger.isDebugEnabled())
			logger.debug("ServletContext start with {} ms", (System.currentTimeMillis() - start));
	}

	@Override
	public void filter(Class<?> clazz) {
		if (clazz != null) {
			classes.add(clazz);
			// 判断是不是ActionSupport类
			if (ClassUtils.isController(clazz)) {
				actionWrapper.bindActions(clazz);
			} else if (ClassUtils.isFilter(clazz)) {
				filters.add(clazz);
			} else if (ClassUtils.isWebInitializer(clazz)) {
				initializers.add(clazz);
			}
		}
	}
}
