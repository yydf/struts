package cn.coder.struts.support;

import java.util.ArrayList;
import java.util.regex.Pattern;

import cn.coder.struts.util.StringUtils;

/**
 * 表单验证的基础类
 * 
 * @author YYDF
 *
 */
public abstract class DataValidator {

	private ActionSupport support;
	private ArrayList<String> errors;
	private static final Pattern PATTERN_MOBILE = Pattern
			.compile("^((13[0-9])|(15[^4,\\D])|(17[0-9])|(18[0-9]))\\d{8}$");
	protected static final String ALL = "all";
	protected static final String SINGLE = "single";

	public boolean validate(ActionSupport support) {
		errors = new ArrayList<>();
		this.support = support;
		checkList();
		return errors.isEmpty();
	}

	protected void isRequired(String field, String errMsg) {
		if (SINGLE.equals(scope()) && !errors.isEmpty())
			return;
		if (StringUtils.isEmpty(support.getParameter(field))) {
			this.errors.add(errMsg);
		}
	}

	protected void isMobile(String field, String errMsg) {
		if (SINGLE.equals(scope()) && !errors.isEmpty())
			return;
		if (!PATTERN_MOBILE.matcher(support.getParameter(field)).matches()) {
			this.errors.add(errMsg);
		}
	}

	protected void isPattern(String field, String errMsg, String pattern) {
		if (SINGLE.equals(scope()) && !errors.isEmpty())
			return;
		Pattern regex = Pattern.compile(pattern);
		if (!regex.matcher(support.getParameter(field)).matches()) {
			this.errors.add(errMsg);
		}
	}

	protected abstract String scope();

	protected abstract void checkList();

	public String getErrors() {
		StringBuilder sb = new StringBuilder();
		for (String error : errors) {
			sb.append(error).append("\n");
		}
		return sb.toString();
	}

}
