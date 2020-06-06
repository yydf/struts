package cn.coder.struts;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.annotation.WebInit;
import cn.coder.struts.event.StrutsEventListener;
import cn.coder.struts.handler.Handler;
import cn.coder.struts.handler.HandlerAdapter;
import cn.coder.struts.handler.MatchableURIHandler;
import cn.coder.struts.handler.SimpleHandlerAdapter;
import cn.coder.struts.handler.SimpleURIHandler;
import cn.coder.struts.util.BeanUtils;
import cn.coder.struts.view.JSONView;
import cn.coder.struts.view.JSPView;
import cn.coder.struts.view.DefaultView;
import cn.coder.struts.view.View;
import cn.coder.struts.wrapper.BeanWrapper;

public final class StrutsApplicationContext {
	private static final Logger logger = LoggerFactory.getLogger(StrutsApplicationContext.class);

	private ServletContext sc;
	private List<Method> webInits;
	private List<Handler> handlers;
	private List<HandlerAdapter> adapters;
	private List<View> views;
	private List<StrutsEventListener> listeners;
	private BeanWrapper beanWrapper;

	private static final String ATTRIBUTE_APPLICATION_CONTEXT = StrutsApplicationContext.class.getName() + ".CONTEXT";

	public StrutsApplicationContext(FilterConfig filterConfig) {
		init(filterConfig);
		doWebInit();
		defaultHandlers();
		defaultAdapters();
		defaultViews();
		defaultListeners();
	}

	private void init(FilterConfig filterConfig) {
		this.sc = filterConfig.getServletContext();
		this.sc.setAttribute(ATTRIBUTE_APPLICATION_CONTEXT, this);
		List<String> beanNames = new ArrayList<>();
		doScan("/WEB-INF/", beanNames);
		this.beanWrapper = new BeanWrapper(beanNames);
	}

	private void doWebInit() {
		this.webInits = new ArrayList<>();
		List<Class<?>> temp = this.getBeanNamesByAnnotation(WebInit.class);
		WebInit webInit;
		for (Class<?> clazz : temp) {
			webInit = clazz.getAnnotation(WebInit.class);
			try {
				clazz.getDeclaredMethod(webInit.init()).invoke(this.getBean(clazz.getName()));
				// 将销毁方法加入到缓存，方便调用
				this.webInits.add(clazz.getDeclaredMethod(webInit.destroy()));
			} catch (Exception e) {
				logger.warn("Call the method '" + webInit.init() + "' of '" + clazz + "' faild", e);
			}
		}
	}

	private void doScan(String path, List<String> beanNames) {
		Set<String> paths = this.sc.getResourcePaths(path);
		if (paths != null && !paths.isEmpty()) {
			for (String temp : paths) {
				if (temp.endsWith("/"))
					doScan(temp, beanNames);
				else {
					if (temp.endsWith(".class")) {
						temp = BeanUtils.toBeanName(temp);
						beanNames.add(temp);
					}
				}
			}
		}
	}

	private void defaultHandlers() {
		this.handlers = new ArrayList<>();
		this.handlers.add(new SimpleURIHandler(this));
		this.handlers.add(new MatchableURIHandler(this));
		findBeans(Handler.class, this.handlers, true);
	}

	private void defaultAdapters() {
		this.adapters = new ArrayList<>();
		this.adapters.add(new SimpleHandlerAdapter(this));
		findBeans(HandlerAdapter.class, this.adapters, true);
	}

	private void defaultViews() {
		this.views = new ArrayList<>();
		this.views.add(new JSONView());
		this.views.add(new JSPView());
		findBeans(View.class, this.views, false);
		// 默认View放在最后
		this.views.add(new DefaultView());
	}

	private void defaultListeners() {
		this.listeners = new ArrayList<>();
		findBeans(StrutsEventListener.class, this.listeners, false);
	}

	@SuppressWarnings("unchecked")
	private <T> void findBeans(Class<?> type, List<T> list, boolean initWithContext) {
		List<Class<?>> classList = this.getBeanNamesByType(type);
		for (Class<?> clazz : classList) {
			try {
				Object obj;
				if (initWithContext)
					obj = clazz.getConstructor(StrutsApplicationContext.class).newInstance(this);
				else
					obj = clazz.newInstance();
				list.add((T) obj);
			} catch (Exception e) {
				logger.warn("Create bean of '" + clazz + "' faild", e);
			}
		}
	}

	public List<Handler> getHandlers() {
		return this.handlers;
	}

	public List<HandlerAdapter> getAdapters() {
		return this.adapters;
	}

	public List<View> getViews() {
		return this.views;
	}

	public List<StrutsEventListener> getListeners() {
		return this.listeners;
	}

	public Object getBean(String beanName) {
		return this.beanWrapper.getSingleton(beanName);
	}

	public List<Class<?>> getBeanNamesByType(Class<?> type) {
		return this.beanWrapper.getBeanNamesByType(type);
	}

	public List<Class<?>> getBeanNamesByAnnotation(Class<? extends Annotation> type) {
		return this.beanWrapper.getBeanNamesByAnnotation(type);
	}

	public synchronized void clear() {
		if (this.webInits != null) {
			String beanName;
			for (Method method : this.webInits) {
				beanName = method.getDeclaringClass().getName();
				try {
					method.invoke(this.getBean(beanName));
				} catch (Exception e) {
					logger.warn("Call the method '" + method.getName() + "' of '" + beanName + "' faild", e);
				}
			}
			this.webInits.clear();
			this.webInits = null;
		}
		this.handlers = null;
		this.adapters = null;
		this.views = null;
		this.beanWrapper.clear();
		if (this.sc != null) {
			this.sc.removeAttribute(ATTRIBUTE_APPLICATION_CONTEXT);
			this.sc = null;
		}
	}

}
