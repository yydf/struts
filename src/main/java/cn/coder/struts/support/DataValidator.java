package cn.coder.struts.support;

import java.util.ArrayList;

import cn.coder.struts.util.StringUtils;

public abstract class DataValidator {

	private ActionSupport support;
	private ArrayList<String> errors;

	public boolean validate(ActionSupport support) {
		errors = new ArrayList<>();
		this.support = support;
		checkList();
		return errors.isEmpty();
	}

	protected void isRequired(String field, String errMsg) {
		if (StringUtils.isEmpty(support.getParameter(field))) {
			this.errors.add(errMsg);
		}
	}

	protected abstract void checkList();

	public String getErrors() {
		StringBuilder sb = new StringBuilder();
		for (String error : errors) {
			sb.append(error).append("\n");
		}
		return sb.toString();
	}

}
