package cn.coder.struts.aop;

public final class Aop {

	private static final AopFactory factory = new AopFactory();

	/**
	 * 创建一个对象
	 * @param controller
	 * @return
	 */
	public static <T> T create(Class<T> controller) {
		return factory.create(controller);
	}

	/**
	 * 注入存在Resource注解的对象
	 * @param obj
	 */
	public static void inject(Object obj) {
		factory.inject(obj);
	}

}
