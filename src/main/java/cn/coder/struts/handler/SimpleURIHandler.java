package cn.coder.struts.handler;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import cn.coder.struts.StrutsApplicationContext;
import cn.coder.struts.annotation.Request;
import cn.coder.struts.annotation.Skip;
import cn.coder.struts.util.BeanUtils;

public class SimpleURIHandler extends AbstractHandler {
	
	public SimpleURIHandler(StrutsApplicationContext context) {
		super(context);
	}
	
	@Override
	protected HandlerMethod getHandlerMethod(HttpServletRequest req) {
		return this.handlerMethods.get(req.getServletPath());
	}

	@Override
	protected void registerHandler(Class<?> temp) {
		Method[] methods = temp.getDeclaredMethods();
		Request r1 = temp.getAnnotation(Request.class);
		String path;
		HandlerMethod hm;
		for (Method method : methods) {
			Request r2 = method.getAnnotation(Request.class);
			if (r2 != null) {
				path = BeanUtils.genericPath(r1, r2);
				hm = new HandlerMethod(method);
				hm.setSkip(temp.getAnnotation(Skip.class) != null || method.getAnnotation(Skip.class) != null);
				this.handlerMethods.put(path, hm);
			}
		}
	}
	
}
