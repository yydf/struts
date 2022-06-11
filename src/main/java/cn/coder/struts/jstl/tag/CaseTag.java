package cn.coder.struts.jstl.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class CaseTag extends BodyTagSupport {
	private static final long serialVersionUID = 5738376830292291109L;
	private boolean isOk;

	public int doStartTag() throws JspException {
		this.isOk = false;
		return 1;
	}

	public boolean isOk() {
		return this.isOk;
	}

	public void setOk(boolean isOk) {
		this.isOk = isOk;
	}
}
