package cn.coder.struts.aop;

import cn.coder.struts.support.ProxyHandler;

public final class Aop {

	private static final AopFactory factory = new AopFactory();

	/**
	 * 创建一个对象
	 * 
	 * @param clazz
	 *            类型
	 * @return 创建的对象
	 */
	public static <T> T create(Class<T> clazz) {
		if (clazz == null)
			return null;
		return factory.create(clazz);
	}

	/**
	 * 注入存在Resource注解的对象
	 * 
	 * @param obj
	 *            要注入的对象
	 */
	public static void inject(Object obj) {
		if (obj == null)
			return;
		factory.inject(obj);
	}

	public static Object getProxy(Class<?> clazz, Class<? extends ProxyHandler> handler) {
		if (clazz == null)
			return null;
		if (handler == null)
			return null;
		Class<?>[] interfaces = clazz.getInterfaces();
		if (interfaces.length == 0)
			throw new NullPointerException("Not found thd interfaces");
		return factory.getProxyedObject(clazz, handler, interfaces);
	}

	/**
	 * 清空缓存
	 */
	public synchronized static void clear() {
		factory.clear();
	}

}
