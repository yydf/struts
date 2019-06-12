package cn.coder.struts.core;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.aop.Aop;
import cn.coder.struts.support.ActionSupport;
import cn.coder.struts.support.DataValidator;
import cn.coder.struts.util.ClassUtils;
import cn.coder.struts.view.JSONMap;

public final class Action {
	private static final Logger logger = LoggerFactory.getLogger(Action.class);
	private Class<?> controller;
	private Method actionMethod;
	private Class<?> validator;

	public Action(Method method) {
		this.actionMethod = method;
		this.controller = method.getDeclaringClass();
		this.validator = ClassUtils.getValidator(method);
	}

	public Method getMethod() {
		return this.actionMethod;
	}

	public Class<?> getController() {
		return this.controller;
	}

	public Object invoke(ActionSupport support) throws Exception {
		if (validator != null) {
			DataValidator check = (DataValidator) Aop.create(validator);
			if (!check.validate(support)) {
				if (logger.isDebugEnabled())
					logger.debug("Action stoped by validator '{}'", validator.getName());
				return JSONMap.error(100, check.getErrors());
			}
		}
		return actionMethod.invoke(support);
	}

}
