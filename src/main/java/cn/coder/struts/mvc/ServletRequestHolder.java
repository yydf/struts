package cn.coder.struts.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class ServletRequestHolder {

	private static final ThreadLocal<HttpServletRequest> requestLocal = new ThreadLocal<>();
	private static final ThreadLocal<HttpServletResponse> responseLocal = new ThreadLocal<>();

	public static void hold(HttpServletRequest request, HttpServletResponse response) {
		requestLocal.set(request);
		responseLocal.set(response);
	}

	public static HttpServletRequest getRequestContext() {
		HttpServletRequest req = requestLocal.get();
		if (req == null)
			throw new NullPointerException("The request context can not be null");
		return req;
	}

	public static HttpServletResponse getResponseContext() {
		HttpServletResponse res = responseLocal.get();
		if (res == null)
			throw new NullPointerException("The response context can not be null");
		return res;
	}

}
