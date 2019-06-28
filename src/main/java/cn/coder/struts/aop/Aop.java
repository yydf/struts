package cn.coder.struts.aop;

public final class Aop {

	private static final AopFactory factory = new AopFactory();

	/**
	 * 创建一个对象
	 * 
	 * @param controller
	 * @return
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
	 */
	public static void inject(Object obj) {
		if (obj == null)
			return;
		factory.inject(obj);
	}

	/**
	 * 清空缓存
	 */
	public static void clear() {
		factory.clear();
	}

}
