package cn.coder.struts.wrapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.coder.struts.annotation.Order;

/**
 * 根据@Order注解进行排序
 * 
 * @author YYDF
 * @param <E>
 *
 */
public final class OrderWrapper<E> implements Comparator<E> {

	@Override
	public int compare(E o1, E o2) {
		// 判断@Order注解的大小
		return getOrderNum(o1).compareTo(getOrderNum(o2));
	}

	private static Integer getOrderNum(Object arg0) {
		Order order0 = arg0.getClass().getAnnotation(Order.class);
		if (order0 != null)
			return order0.value();
		return 0;
	}

	public static <E> void sort(List<E> temp) {
		if(temp != null && temp.size() > 1){
			Collections.sort(temp, new OrderWrapper<>());
		}
	}

}
