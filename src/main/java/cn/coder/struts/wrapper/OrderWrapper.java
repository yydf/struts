package cn.coder.struts.wrapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.coder.struts.annotation.Order;

/**
 * 根据@Order注解进行排序
 * 
 * @author YYDF
 *
 */
public class OrderWrapper implements Comparator<Class<?>> {

	@Override
	public int compare(Class<?> o1, Class<?> o2) {
		// 判断@Order注解的大小
		return getOrderNum(o1).compareTo(getOrderNum(o2));
	}

	private static Integer getOrderNum(Class<?> arg0) {
		Order order0 = arg0.getAnnotation(Order.class);
		if (order0 != null)
			return order0.value();
		return 0;
	}

	public static void sort(List<Class<?>> classes) {
		if (classes != null && classes.size() > 1) {
			Collections.sort(classes, new OrderWrapper());
		}
	}

}
