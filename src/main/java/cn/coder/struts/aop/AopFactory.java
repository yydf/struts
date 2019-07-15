package cn.coder.struts.aop;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.support.ProxyHandler;
import cn.coder.struts.util.BeanUtils;

public final class AopFactory {
	private static final Logger logger = LoggerFactory.getLogger(AopFactory.class);

	private static ArrayList<Class<?>> allClasses;
	private final HashMap<Field, Class<?>> maps = new HashMap<>();
	private final ArrayList<Class<?>> noInject = new ArrayList<>();

	public synchronized static void init(ArrayList<Class<?>> classes) {
		allClasses = classes;
	}

	public <T> T create(final Class<T> controller) {
		try {
			T obj = controller.newInstance();
			inject(obj);
			return obj;
		} catch (Exception e) {
			throw new NullPointerException("Can not create bean '" + controller.getName() + "'");
		}
	}

	public void inject(final Object obj) {
		if (obj == null)
			return;
		Class<?> clazz = obj.getClass();
		// 如果没有需要注入的属性
		if (noInject.contains(clazz))
			return;
		Set<Field> fields = BeanUtils.getDeclaredFields(clazz);
		int hasInject = 0;
		for (Field field : fields) {
			if (field.getAnnotation(Resource.class) != null) {
				Class<?> target = maps.get(field);
				if (target == null) {
					target = findBean(field, obj);
					maps.put(field, target);
				}
				BeanUtils.setValue(field, obj, create(target));
				hasInject++;
			}
		}

		// 将没有Resource注解的对象存入缓存
		if (hasInject == 0) {
			noInject.add(clazz);
		}
	}

	public Object getProxyedObject(Class<?> clazz, Class<? extends ProxyHandler> handler, Class<?>[] interfaces) {
		Object obj = create(clazz);
		if (obj != null)
			return create(handler).bind(obj, interfaces);
		throw new NullPointerException("Can't get the proxyobj");
	}

	private Class<?> findBean(Field field, Object obj) {
		for (Class<?> cla : allClasses) {
			if (field.getType().isAssignableFrom(cla)) {
				BeanUtils.setValue(field, obj, create(cla));
				return cla;
			}
		}
		throw new NullPointerException("Can not find resource '" + field.getName() + "'");
	}

	public void clear() {
		maps.clear();
		noInject.clear();
		if (allClasses != null) {
			allClasses.clear();
			allClasses = null;
		}
		if (logger.isDebugEnabled())
			logger.debug("Aop factory cleared");
	}

}
