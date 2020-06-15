package cn.coder.struts.handler;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cn.coder.struts.StrutsApplicationContext;
import cn.coder.struts.annotation.Skip;
import cn.coder.struts.mvc.Controller;
import cn.coder.struts.mvc.Interceptor;
import cn.coder.struts.mvc.RequestInterceptor;

public abstract class AbstractRequestMethodHandler implements Handler {

	private StrutsApplicationContext sac;
	private final Map<String, HandlerMethod> handlerMethods = new LinkedHashMap<>();

	protected AbstractRequestMethodHandler(StrutsApplicationContext context) {
		this.sac = context;
		detectHandlers();
	}

	private void detectHandlers() {
		Class<?>[] beanNames = getApplicationContext().getBeanNamesByType(Controller.class);
		for (Class<?> clazz : beanNames) {
			registerHandler(clazz);
		}
	}

	protected abstract void registerHandler(Class<?> clazz);

	@Override
	public HandlerChain getHandlerChain(HttpServletRequest req) {
		Object handler = lookupHandler(req);
		if (handler == null)
			return null;
		return getHandlerChain(handler, req);
	}

	protected abstract Object lookupHandler(HttpServletRequest req);

	private HandlerChain getHandlerChain(Object handler, HttpServletRequest req) {
		HandlerChain chain;
		if (handler instanceof HandlerChain)
			chain = (HandlerChain) handler;
		else
			chain = new HandlerChain(handler);

		String lookupPath = req.getServletPath();
		List<Interceptor> interceptors = getApplicationContext().getInterceptors();
		for (Interceptor temp : interceptors) {
			if (temp instanceof RequestInterceptor) {
				if (((RequestInterceptor) temp).matches(lookupPath)) {
					chain.addInterceptor(temp);
				}
			}else{
				chain.addInterceptor(temp);
			}
		}
		return chain;
	}

	protected static boolean matchablePath(String path) {
		return path.contains("{") && path.contains("}");
	}

	protected StrutsApplicationContext getApplicationContext() {
		return this.sac;
	}

	protected Map<String, HandlerMethod> getHandlerMethods() {
		return this.handlerMethods;
	}

	protected static boolean getSkip(Class<?> clazz, Method method) {
		return clazz.getAnnotation(Skip.class) != null || method.getAnnotation(Skip.class) != null;
	}
}
