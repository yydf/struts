package cn.coder.struts.jstl.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CutStringTag extends BodyTagSupport {
	private static final long serialVersionUID = -327645925586914122L;
	private static final Logger logger = LoggerFactory.getLogger(CutStringTag.class);
	private String str;
	private int len;

	public void setStr(String str) {
		this.str = str;
	}

	public void setLen(int len) {
		this.len = len;
	}

	public int doStartTag() throws JspException {
		return 2;
	}

	public int doEndTag() throws JspException {
		if ((this.str != null) && (this.str.length() > this.len)) {
			this.str = this.str.substring(0, this.len);
		}
		try {
			this.pageContext.getOut().append(this.str);
		} catch (IOException e) {
			logger.error("输出substr标签失败", e);
		}
		return 6;
	}
}
