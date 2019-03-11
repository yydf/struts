package cn.coder.struts.util;

public class Assert {

	public static void notNull(Object obj, String str) {
		if (obj == null)
			throw new NullPointerException("The " + str + " can not be null");
	}

}
