package cn.coder.struts.core;

import java.util.ArrayList;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.aop.Aop;
import cn.coder.struts.aop.AopFactory;
import cn.coder.struts.support.WebInitializer;
import cn.coder.struts.wrapper.ActionWrapper;
import cn.coder.struts.wrapper.OrderWrapper;

/**
 * Struts核心类(初始化和绑定)
 * 
 * @author YYDF
 *
 */
public final class StrutsContext {
	private static final Logger logger = LoggerFactory.getLogger(StrutsContext.class);
	private ServletContext sc;
	private ActionHandler handler;
	private ArrayList<WebInitializer> initArray;

	public synchronized void init(ServletContext servletContext) {
		this.sc = servletContext;
		initAopFactory();
		addMapping();
		initWebInitializer();
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

	private void initWebInitializer() {
		@SuppressWarnings("unchecked")
		ArrayList<Class<?>> initClasses = (ArrayList<Class<?>>) sc.getAttribute("InitClasses");
		sc.removeAttribute("InitClasses");
		if (initClasses != null && initClasses.size() > 0) {
			OrderWrapper.sort(initClasses);
			initArray = new ArrayList<>();
			WebInitializer initObj;
			for (Class<?> clazz : initClasses) {
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
		@SuppressWarnings("unchecked")
		ArrayList<Class<?>> filters = (ArrayList<Class<?>>) sc.getAttribute("Filters");
		sc.removeAttribute("Filters");
		// 按Order注解排序
		OrderWrapper.sort(filters);
		ActionWrapper actionWrapper = (ActionWrapper) sc.getAttribute("ActionWrapper");
		sc.removeAttribute("ActionWrapper");
		this.handler = new ActionHandler(actionWrapper, filters);
	}

	public synchronized void startUp() {
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

	public synchronized void destroy() {
		if (initArray != null) {
			for (WebInitializer init : initArray) {
				init.destroy();
			}
			initArray.clear();
		}
		this.sc = null;
		this.handler.clear();
		this.handler = null;
	}

}
