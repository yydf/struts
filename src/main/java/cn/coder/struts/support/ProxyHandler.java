package cn.coder.struts.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProxyHandler implements InvocationHandler {
	private static final Logger logger = LoggerFactory.getLogger(ProxyHandler.class);

	private Object proxyObj;

	public Object bind(Object obj, Class<?>[] interfaces) {
		this.proxyObj = obj;
		return Proxy.newProxyInstance(obj.getClass().getClassLoader(), interfaces, this);
	}

	public abstract void beforeInvoke(Object proxy, Method method, Object[] args);

	public abstract void afterInvoke(Object result, Method method, Object[] args);

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (logger.isDebugEnabled())
			logger.debug("'{}' before invoke", method.getName());
		beforeInvoke(proxy, method, args);
		Object result = method.invoke(this.proxyObj, args);
		if (logger.isDebugEnabled())
			logger.debug("'{}' after invoke", method.getName());
		afterInvoke(result, method, args);
		return result;
	}

}