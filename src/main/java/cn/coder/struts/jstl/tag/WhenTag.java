package cn.coder.struts.jstl.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhenTag extends BodyTagSupport {
	private static final long serialVersionUID = 4778987987915422993L;
	private static final Logger logger = LoggerFactory.getLogger(WhenTag.class);
	private String test;

	public void setTest(String test) {
		this.test = test;
	}

	public int doStartTag() throws JspException {
		return 2;
	}

	public int doEndTag() throws JspException {
		CaseTag parent = (CaseTag) getParent();
		if ((Boolean.parseBoolean(this.test)) && (!parent.isOk())) {
			try {
				String content = getBodyContent().getString();

				this.pageContext.getOut().append(content.trim());
				parent.setOk(true);
			} catch (IOException e) {
				logger.error("输出when标签失败", e);
			}
		}
		return 6;
	}
}
