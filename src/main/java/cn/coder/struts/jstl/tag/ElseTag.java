package cn.coder.struts.jstl.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElseTag extends BodyTagSupport {
	private static final long serialVersionUID = -7339585316460624996L;
	private static final Logger logger = LoggerFactory.getLogger(ElseTag.class);

	public int doStartTag() throws JspException {
		return 2;
	}

	public int doEndTag() throws JspException {
		CaseTag parent = (CaseTag) getParent();
		if (!parent.isOk()) {
			try {
				String content = getBodyContent().getString();

				this.pageContext.getOut().append(content.trim());
				parent.setOk(true);
			} catch (IOException e) {
				logger.error("输出else标签失败", e);
			}
		}
		return 6;
	}
}