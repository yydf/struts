package cn.coder.struts;

import java.util.ArrayList;
import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.support.WebInitializer;
import cn.coder.struts.util.ClassUtils;
import cn.coder.struts.util.ClassUtils.FilterClassType;
import cn.coder.struts.wrapper.ActionWrapper;
import cn.coder.struts.wrapper.SessionWrapper;

/**
 * 获取WebInitializer接口实现类<br>
 * 扫描WEB-INF目录下的所有class文件<br>
 * 查找所有的Action和Intercepter
 * 
 * @author YYDF 2019-05-21
 *
 */
@HandlesTypes(WebInitializer.class)
public class StrutsContainerInitializer implements ServletContainerInitializer, FilterClassType {
	private static final Logger logger = LoggerFactory.getLogger(StrutsContainerInitializer.class);
	private final ActionWrapper actionWrapper = new ActionWrapper();
	private final ArrayList<Class<?>> classes = new ArrayList<>();
	private final ArrayList<Class<?>> filters = new ArrayList<>();

	public void onStartup(Set<Class<?>> initClasses, ServletContext ctx) throws ServletException {
		long start = System.nanoTime();
		ClassUtils.scanClasses(ctx, "/", this);
		ctx.setAttribute("ActionWrapper", actionWrapper);
		ctx.setAttribute("Classes", classes);
		ctx.setAttribute("Filters", filters);
		ctx.setAttribute("InitClasses", new ArrayList<>(initClasses));

		// 增加session处理类
		ctx.addListener(SessionWrapper.class);
		ctx.addFilter("StrutsFilter", StrutsFilter.class);
		if (logger.isDebugEnabled())
			logger.debug("ServletContext start up:{}ns", (System.nanoTime() - start));
	}

	@Override
	public void filter(Class<?> clazz) {
		if (clazz != null) {
			if (ClassUtils.isController(clazz)) {
				actionWrapper.bindActions(clazz);
			} else if (ClassUtils.isFilter(clazz)) {
				filters.add(clazz);
			}
			classes.add(clazz);
		}
	}
}
