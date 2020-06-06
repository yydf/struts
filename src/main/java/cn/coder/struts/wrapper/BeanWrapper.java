package cn.coder.struts.wrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.util.BeanUtils;

public final class BeanWrapper {
	private static final Logger logger = LoggerFactory.getLogger(BeanWrapper.class);

	private List<String> beanNames;
	private Map<String, Object> singletonObjects;

	public BeanWrapper(List<String> beans) {
		this.beanNames = beans;
		this.singletonObjects = new ConcurrentHashMap<>();
	}

	public Object getSingleton(String beanName) {
		synchronized (this.singletonObjects) {
			// 检查缓存中是否存在实例
			Object singletonObject = this.singletonObjects.get(beanName);
			if (singletonObject == null) {
				try {
					Class<?> clazz = Class.forName(beanName);
					singletonObject = clazz.newInstance();
					Field[] fields = BeanUtils.getDeclaredFields(clazz);
					for (Field field : fields) {
						if (!Modifier.isFinal(field.getModifiers()) && field.getAnnotation(Resource.class) != null) {
							if (!field.isAccessible())
								field.setAccessible(true);
							field.set(singletonObject, getSingleton(field.getType().getName()));
						}
					}
				} catch (Exception e) {
					if (logger.isWarnEnabled())
						logger.warn("Create bean of '{}' faild", beanName);
				}
				// 如果实例对象在不存在，我们注册到单例注册表中。
				this.singletonObjects.put(beanName, singletonObject);
			}
			return singletonObject;
		}
	}

	public List<Class<?>> getBeanNamesByType(Class<?> type) {
		List<Class<?>> temp = new ArrayList<>();
		if (!this.beanNames.isEmpty()) {
			Class<?> c1;
			for (String beanName : this.beanNames) {
				c1 = BeanUtils.toClass(beanName);
				if (c1 != null && type.isAssignableFrom(c1))
					temp.add(c1);
			}
		}
		return temp;
	}

	public void clear() {
		this.beanNames.clear();
		this.beanNames = null;
		this.singletonObjects.clear();
		this.singletonObjects = null;
	}

}
