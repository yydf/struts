package cn.coder.struts.holder;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.support.ServletWebRequest;

public abstract class RequestHolder {
	
	private static final ThreadLocal<ServletWebRequest> temp = new ThreadLocal<>();

	public static void hold(ServletRequest request, ServletResponse response) {
		temp.set(new ServletWebRequest((HttpServletRequest)request, (HttpServletResponse)response));
	}

	public static ServletWebRequest getContext() {
		ServletWebRequest req = temp.get();
		if(req == null)
			throw new NullPointerException("The request context can not be null");
		return req;
	}

}
