package cn.coder.struts.handler;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import cn.coder.struts.StrutsApplicationContext;
import cn.coder.struts.annotation.Request;
import cn.coder.struts.util.BeanUtils;

public final class SimpleRequestMethodHandler extends AbstractRequestMethodHandler {

	public SimpleRequestMethodHandler(StrutsApplicationContext context) {
		super(context);
	}

	@Override
	protected Object lookupHandler(HttpServletRequest req) {
		return getHandlerMethods().get(req.getServletPath());
	}

	@Override
	protected void registerHandler(Class<?> clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		Request r1 = clazz.getAnnotation(Request.class);
		String path;
		for (Method method : methods) {
			path = BeanUtils.genericPath(r1, method.getAnnotation(Request.class));
			if (!matchablePath(path)) {
				boolean skip = getSkip(clazz, method);
				Object bean = getApplicationContext().getBean(clazz.getName());
				getHandlerMethods().put(path, new HandlerMethod(bean, method, skip));
			}
		}
	}
}
