package cn.coder.struts.event;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.StrutsFilter;

public final class ServletRequestHandleEvent {

	private Throwable error;
	private HttpServletRequest req;
	private HttpServletResponse res;
	private Object resultValue;
	private long processStartTime;
	private StrutsFilter dispatcher;

	public ServletRequestHandleEvent(StrutsFilter strutsFilter, HttpServletRequest request,
			HttpServletResponse response, long startTime, Object result, Throwable dispatchError) {
		this.error = dispatchError;
		this.req = request;
		this.res = response;
		this.resultValue = result;
		this.processStartTime = startTime;
		this.dispatcher = strutsFilter;
	}

	public Throwable getError() {
		return this.error;
	}

	public HttpServletRequest getRequest() {
		return this.req;
	}

	public HttpServletResponse getResponse() {
		return this.res;
	}

	public Object getResultValue() {
		return this.resultValue;
	}

	public long getProcessStartTime() {
		return this.processStartTime;
	}

	public StrutsFilter getDispatcher() {
		return this.dispatcher;
	}

}
