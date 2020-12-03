package cn.coder.struts.support;

import cn.coder.struts.core.Invocation;

public abstract class Interceptor {

	public abstract void intercept(Invocation inv);
	
}
