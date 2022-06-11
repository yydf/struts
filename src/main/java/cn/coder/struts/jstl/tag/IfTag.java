package cn.coder.struts.jstl.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfTag extends BodyTagSupport {
	private static final long serialVersionUID = -327645925586914122L;
	private static final Logger logger = LoggerFactory.getLogger(IfTag.class);
	private String test;

	public void setTest(String test) {
		this.test = test;
	}

	public int doStartTag() throws JspException {
		return 2;
	}

	public int doEndTag() throws JspException {
		String content = getBodyContent().getString();
		if (Boolean.parseBoolean(this.test)) {
			try {
				this.pageContext.getOut().append(content.trim());
			} catch (IOException e) {
				logger.error("输出if标签失败", e);
			}
		}
		return 6;
	}
}
