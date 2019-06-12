package cn.coder.struts.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.annotation.Order;
import cn.coder.struts.aop.Aop;
import cn.coder.struts.aop.AopFactory;
import cn.coder.struts.support.ActionIntercepter;
import cn.coder.struts.support.WebInitializer;
import cn.coder.struts.wrapper.ActionWrapper;

public final class StrutsContext {
	private static final Logger logger = LoggerFactory.getLogger(StrutsContext.class);
	private ServletContext sc;
	private ActionHandler handler;
	private ArrayList<WebInitializer> initArray;
	private ArrayList<ActionIntercepter> filters;

	public void init(ServletContext servletContext) {
		this.sc = servletContext;
		initAopFactory();
		addMapping();
		initWebInitializer();
		initFilter();
		initActionHandler();
	}

	private void addMapping() {
		EnumSet<DispatcherType> dispatcherTypes = EnumSet.allOf(DispatcherType.class);
		dispatcherTypes.add(DispatcherType.REQUEST);
		dispatcherTypes.add(DispatcherType.FORWARD);
		sc.getFilterRegistration("StrutsFilter").addMappingForUrlPatterns(dispatcherTypes, true, "/*");
	}

	private void initAopFactory() {
		@SuppressWarnings("unchecked")
		ArrayList<Class<?>> classes = (ArrayList<Class<?>>) sc.getAttribute("Classes");
		sc.removeAttribute("Classes");
		AopFactory.init(classes);
	}

	@SuppressWarnings("unchecked")
	private void initFilter() {
		filters = (ArrayList<ActionIntercepter>) sc.getAttribute("Filters");
		sc.removeAttribute("Filters");
		if (filters != null && filters.size() > 1) {
			// 按Order注解排序
			Collections.sort(filters, new Comparator<ActionIntercepter>() {
				@Override
				public int compare(ActionIntercepter arg0, ActionIntercepter arg1) {
					Integer o1 = 0, o2 = 0;
					Order order0 = arg0.getClass().getAnnotation(Order.class);
					if (order0 != null)
						o1 = order0.value();
					Order order1 = arg1.getClass().getAnnotation(Order.class);
					if (order1 != null)
						o2 = order1.value();
					return o1.compareTo(o2);
				}
			});
		}
	}

	private void initWebInitializer() {
		@SuppressWarnings("unchecked")
		Set<Class<?>> InitializerClasses = (Set<Class<?>>) sc.getAttribute("InitializerClasses");
		sc.removeAttribute("InitializerClasses");
		if (InitializerClasses != null && InitializerClasses.size() > 0) {
			initArray = new ArrayList<>();
			WebInitializer initObj;
			for (Class<?> clazz : InitializerClasses) {
				try {
					initObj = (WebInitializer) clazz.newInstance();
					initArray.add(initObj);
				} catch (Exception e) {
					logger.error("WebInitializer create faild", e);
				}
			}
		}
	}

	private void initActionHandler() {
		ActionWrapper actionWrapper = (ActionWrapper) sc.getAttribute("ActionWrapper");
		sc.removeAttribute("ActionWrapper");
		this.handler = new ActionHandler(actionWrapper, filters);
	}

	public void startUp() {
		if (initArray != null) {
			for (WebInitializer init : initArray) {
				Aop.inject(init);
				init.onStartup(sc);
			}
		}
	}

	public ActionHandler getHandler() {
		return this.handler;
	}

	public void destroy() {
		if (initArray != null) {
			for (WebInitializer init : initArray) {
				init.destroy();
			}
			initArray.clear();
		}
		if (filters != null) {
			filters.clear();
		}
		this.sc = null;
		this.handler = null;
	}

}
