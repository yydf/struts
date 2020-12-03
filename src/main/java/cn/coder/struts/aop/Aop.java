package cn.coder.struts.aop;

public final class Aop {

	private static final AopFactory factory = new AopFactory();

	/**
	 * 创建一个对象
	 * 
	 * @param clazz 类型
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
	 * @param obj 要注入的对象
	 */
	public static void inject(Object obj) {
		if (obj == null)
			return;
		factory.inject(obj);
	}

	/**
	 * 清空缓存
	 */
	public synchronized static void clear() {
		factory.clear();
	}

}
