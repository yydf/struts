package cn.coder.struts.core;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class ServletRequestHolder {
	private static final ThreadLocal<WebServletRequest> local = new ThreadLocal<>();

	public static void hold(ServletRequest request, ServletResponse response) {
		local.set(new WebServletRequest(request, response));
	}

	public static HttpServletRequest getRequest() {
		return local.get().getRequest();
	}

	public static HttpServletResponse getResponse() {
		return local.get().getResponse();
	}

	private static final class WebServletRequest {

		private HttpServletRequest req;
		private HttpServletResponse res;

		public WebServletRequest(ServletRequest request, ServletResponse response) {
			this.req = (HttpServletRequest) request;
			this.res = (HttpServletResponse) response;
		}

		public HttpServletRequest getRequest() {
			return this.req;
		}

		public HttpServletResponse getResponse() {
			return this.res;
		}

	}
}
