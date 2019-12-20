package cn.coder.struts.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.handler.Handler;
import cn.coder.struts.handler.HandlerAdapter;
import cn.coder.struts.handler.SimpleRequestAdapter;
import cn.coder.struts.handler.SimpleRequestHandler;
import cn.coder.struts.util.BeanUtils;
import cn.coder.struts.view.JSONMap;
import cn.coder.struts.view.ModelAndView;
import cn.coder.struts.view.TextView;
import cn.coder.struts.view.View;

public class ApplicationContext {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationContext.class);

	private Class<?>[] classes;
	private final ServletContext context;
	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<String, Object>(64);

	public ApplicationContext(ServletContext context) {
		this.context = context;
	}

	public void doScan() {
		List<Class<?>> temp = new ArrayList<>();
		scanClasses(temp, "/WEB-INF/");
		Class<?>[] arr = new Class<?>[temp.size()];
		this.classes = temp.toArray(arr);
	}

	private void scanClasses(List<Class<?>> classes, String path) {
		Set<String> paths = this.context.getResourcePaths(path);
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

	public List<Handler> getHandlers() {
		List<Handler> list = new ArrayList<>();
		list.add(new SimpleRequestHandler(this));
		return list;
	}

	public List<HandlerAdapter> getHandlerAdapters() {
		List<HandlerAdapter> list = new ArrayList<>();
		list.add(new SimpleRequestAdapter(this));
		return list;
	}

	public List<View> getViews() {
		List<View> list = new ArrayList<>();
		list.add(new JSONMap());
		list.add(new TextView());
		list.add(new ModelAndView(null));
		return list;
	}

	public Class<?>[] getClasses() {
		return this.classes;
	}

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
}
