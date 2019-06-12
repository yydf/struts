package cn.coder.struts.core;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.aop.Aop;
import cn.coder.struts.support.ActionIntercepter;
import cn.coder.struts.support.ActionSupport;
import cn.coder.struts.util.ClassUtils;
import cn.coder.struts.wrapper.ActionWrapper;
import cn.coder.struts.wrapper.ResponseWrapper;

public final class ActionHandler {

	private static final Logger logger = LoggerFactory.getLogger(ActionHandler.class);
	private final ActionWrapper wrapper;
	private final ResponseWrapper responseWrapper;
	private final ArrayList<ActionIntercepter> filters;
	private final boolean hasFilter;

	public ActionHandler(ActionWrapper actionWrapper, ArrayList<ActionIntercepter> filterArray) {
		this.wrapper = actionWrapper;
		this.responseWrapper = new ResponseWrapper();
		this.filters = filterArray;
		this.hasFilter = (filterArray != null && !filterArray.isEmpty());
	}

	public Action getAction(String path) {
		return this.wrapper.getAction(path);
	}

	public void handle(Action action, HttpServletRequest req, HttpServletResponse res) {
		if (!checkMethod(action, req, res))
			return;
		if (!checkFilter(req, res))
			return;
		handleAction(action, req, res);
	}

	private void handleAction(Action action, HttpServletRequest req, HttpServletResponse res) {
		ActionSupport support = null;
		try {
			Class<?> controller = action.getController();
			support = (ActionSupport) Aop.create(controller);
			support.init(req, res);
			Object result = action.invoke(support);
			if (result != null) {
				responseWrapper.doResponse(result, req, res);
				if (logger.isDebugEnabled())
					logger.debug("Action handled with response wrapper");
			}
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error("Action handled error", e);
		} finally {
			if (support != null)
				support.clear();
		}
	}

	private boolean checkFilter(HttpServletRequest req, HttpServletResponse res) {
		if (hasFilter) {
			for (ActionIntercepter filter : filters) {
				Aop.inject(filter);
				if (!filter.intercept(req, res)) {
					if (logger.isDebugEnabled())
						logger.debug("Action stoped by filter '{}'", filter.getClass().getName());
					return false;
				}
			}
		}
		return true;
	}

	private static boolean checkMethod(Action action, HttpServletRequest req, HttpServletResponse res) {
		String httpMethod = req.getMethod();
		if (!ClassUtils.allowedHttpMethod(action.getMethod(), httpMethod)) {
			try {
				res.sendError(405, "Request method '" + httpMethod + "' not supported");
				if (logger.isDebugEnabled())
					logger.debug("{} method not allowed", httpMethod);
			} catch (IOException e) {
				if (logger.isErrorEnabled())
					logger.error("Send response error", e);
			}
			return false;
		}
		return true;
	}
}
