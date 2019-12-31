package cn.coder.struts.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.handler.Handler;
import cn.coder.struts.handler.HandlerAdapter;
import cn.coder.struts.handler.MatchableURIHandler;
import cn.coder.struts.handler.SimpleHandlerAdapter;
import cn.coder.struts.handler.SimpleURIHandler;
import cn.coder.struts.util.BeanUtils;
import cn.coder.struts.view.JSONView;
import cn.coder.struts.view.ModelAndView;
import cn.coder.struts.view.TextView;
import cn.coder.struts.view.View;

public class ApplicationContext {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationContext.class);

	private Class<?>[] classes;
	private ServletContext context;
	private ConcurrentHashMap<String, Object> singletonObjects;

	public ApplicationContext(ServletContext context) {
		this.context = context;
		this.singletonObjects = new ConcurrentHashMap<>(64);
	}

	public synchronized void doScan() {
		List<Class<?>> temp = new ArrayList<>();
		scanClasses(temp, "/WEB-INF/");
		Class<?>[] arr = new Class<?>[temp.size()];
		this.classes = temp.toArray(arr);
	}

	private void scanClasses(List<Class<?>> classes, String path) {
		Set<String> paths = this.context.getResourcePaths(path);
		if (paths != null && !paths.isEmpty()) {
			for (String temp : paths) {
				if (temp.endsWith("/"))
					scanClasses(classes, temp);
				else {
					if (temp.endsWith(".class")) {
						Class<?> clazz = BeanUtils.toClass(temp, true);
						if (clazz != null) {
							classes.add(clazz);
						}
					}
				}
			}
		}
	}

	public Handler[] getHandlers() {
		List<Handler> list = new ArrayList<>();
		list.add(new SimpleURIHandler(this));
		list.add(new MatchableURIHandler(this));
		addOtherSupport(list, Handler.class, true);
		Handler[] temp = new Handler[list.size()];
		return list.toArray(temp);
	}

	public HandlerAdapter[] getHandlerAdapters() {
		List<HandlerAdapter> list = new ArrayList<>();
		list.add(new SimpleHandlerAdapter(this));
		addOtherSupport(list, HandlerAdapter.class, true);
		HandlerAdapter[] temp = new HandlerAdapter[list.size()];
		return list.toArray(temp);
	}

	public View[] getViews() {
		List<View> list = new ArrayList<>();
		list.add(new JSONView());
		list.add(new TextView());
		list.add(new ModelAndView());
		addOtherSupport(list, View.class, false);
		View[] temp = new View[list.size()];
		return list.toArray(temp);
	}

	@SuppressWarnings("unchecked")
	private <T> void addOtherSupport(List<T> list, Class<T> class1, boolean initWithContext) {
		if (this.classes.length > 0) {
			for (Class<?> clazz : this.classes) {
				if (class1.isAssignableFrom(clazz)) {
					try {
						Object obj;
						if (initWithContext)
							obj = clazz.getConstructor(ApplicationContext.class).newInstance(this);
						else
							obj = clazz.newInstance();
						list.add((T) obj);
					} catch (Exception e) {
						logger.warn("Add object instanceof '" + class1 + "' faild", e);
					}
				}
			}
		}
	}

	public Class<?>[] getClasses() {
		return this.classes;
	}

	public Class<?>[] getClasses(Class<? extends Annotation> annotation) {
		List<Class<?>> list = new ArrayList<>();
		for (Class<?> clazz : this.classes) {
			if (clazz.getAnnotation(annotation) != null) {
				list.add(clazz);
			}
		}
		Class<?>[] temp = new Class<?>[list.size()];
		return list.toArray(temp);
	}

	public Object getSingleton(Class<?> type) {
		return getSingleton(type.getName());
	}

	/**
	 * 通过beanName获取单例对象
	 * 
	 * @param beanName
	 * @return 单例对象
	 */
	public Object getSingleton(String beanName) {
		synchronized (this.singletonObjects) {
			// 检查缓存中是否存在实例
			Object singletonObject = this.singletonObjects.get(beanName);
			if (singletonObject == null) {
				try {
					Class<?> clazz = Class.forName(beanName);
					singletonObject = clazz.newInstance();
					Field[] fields = clazz.getDeclaredFields();
					for (Field field : fields) {
						if (!Modifier.isFinal(field.getModifiers()) && field.getAnnotation(Resource.class) != null) {
							if (!field.isAccessible())
								field.setAccessible(true);
							field.set(singletonObject, getSingleton(field.getType().getName()));
						}
					}
				} catch (Exception e) {
					if (logger.isWarnEnabled())
						logger.warn("Can not create bean of '{}'", beanName);
				}
				// 如果实例对象在不存在，我们注册到单例注册表中。
				this.singletonObjects.put(beanName, singletonObject);
			}
			return singletonObject;
		}
	}

	public void destroy() {
		if (this.singletonObjects != null) {
			this.singletonObjects.clear();
			this.singletonObjects = null;
		}
		this.classes = null;
		this.context = null;
		if (logger.isDebugEnabled())
			logger.debug("ApplicationContext destroyed");
	}
}
