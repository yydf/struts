package cn.coder.struts.aop;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.util.BeanUtils;

public final class AopFactory {
	private static final Logger logger = LoggerFactory.getLogger(AopFactory.class);
	private static ArrayList<Class<?>> allClasses;
	private HashMap<Field, Class<?>> maps = new HashMap<>();
	private ArrayList<Class<?>> noInject = new ArrayList<>();

	public static void init(ArrayList<Class<?>> classes) {
		allClasses = classes;
	}

	public <T> T create(Class<T> controller) {
		try {
			T obj = controller.newInstance();
			inject(obj);
			if (logger.isDebugEnabled())
				logger.debug("Finished create bean '{}'", controller.getName());
			return obj;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Can not create bean '" + controller.getName() + "'", e);
		}
	}

	public void inject(Object obj) {
		if (obj == null)
			return;
		Class<?> clazz = obj.getClass();
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
				} else {
					injectBean(field, obj, create(target));
				}
				hasInject++;
			}
		}
		// 将没有Resource注解的对象存入缓存
		if (hasInject == 0) {
			noInject.add(clazz);
		}
	}

	private Class<?> findBean(Field field, Object obj) {
		for (Class<?> cla : allClasses) {
			if (field.getType().isAssignableFrom(cla)) {
				injectBean(field, obj, create(cla));
				return cla;
			}
		}
		throw new RuntimeException("Can not find resource '" + field.getName() + "'");
	}

	private static void injectBean(Field field, Object obj, Object target) {
		try {
			BeanUtils.setValue(field, obj, target);
			if (logger.isDebugEnabled())
				logger.debug("Finished inject bean '{}'", field.getType().getName());
		} catch (SecurityException | SQLException e) {
			throw new RuntimeException("Inject bean '" + field.getType().getName() + "' faild", e);
		}
	}

}
