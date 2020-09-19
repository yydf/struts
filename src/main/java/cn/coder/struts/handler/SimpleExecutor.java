package cn.coder.struts.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.mvc.Interceptor;
import cn.coder.struts.mvc.MappedRequest;

public final class SimpleExecutor {
	private static final Logger logger = LoggerFactory.getLogger(SimpleExecutor.class);

	private final Object handler;
	private Interceptor[] interceptors;
	private boolean isEmpty;

	public SimpleExecutor(Object handler) {
		this.handler = handler;
		this.interceptors = new Interceptor[0];
		this.isEmpty = true;
	}

	public Object getHandler() {
		return this.handler;
	}

	public void addInterceptor(Interceptor interceptor) {
		Interceptor[] temp = new Interceptor[this.interceptors.length + 1];
		System.arraycopy(this.interceptors, 0, temp, 0, this.interceptors.length);
		temp[this.interceptors.length] = interceptor;
		this.interceptors = temp;
		if (this.isEmpty) {
			this.isEmpty = false;
		}
	}

	public boolean checkBefore(HttpServletRequest request, HttpServletResponse response) throws Throwable {
		if (this.handler instanceof MappedRequest) {
			// 如果设置了跳过验证
			if (((MappedRequest) this.handler).isSkip())
				return true;
		}
		if (!this.isEmpty) {
			for (Interceptor interceptor : interceptors) {
				try {
					if (!interceptor.before(new Object[] { request, response, this.handler })) {
						return false;
					}
				} catch (Exception ex) {
					interceptor.exceptionCaught(ex);
				}
			}
		}
		return true;
	}

	public void doAfter(Object result) throws Throwable {
		for (Interceptor interceptor : interceptors) {
			try {
				interceptor.after(result);
			} catch (Exception ex) {
				interceptor.exceptionCaught(ex);
				if (logger.isWarnEnabled())
					logger.warn("Stop by exception '" + interceptor + "'", ex);
				break;
			}
		}
	}

}
