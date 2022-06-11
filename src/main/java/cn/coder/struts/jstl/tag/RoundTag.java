package cn.coder.struts.jstl.tag;

import java.io.IOException;
import java.text.DecimalFormat;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoundTag extends BodyTagSupport {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(RoundTag.class);
	private String value;

	public void setValue(String value) {
		this.value = value;
	}

	private int digits = 2;

	public void setDigits(int digits) {
		this.digits = digits;
	}

	public int doStartTag() throws JspException {
		return 2;
	}

	public int doEndTag() throws JspException {
		String format = "#";
		if (this.digits > 0) {
			format = format + ".";
			for (int i = 0; i < this.digits; i++) {
				format = format + "0";
			}
		}
		try {
			try {
				double d = Double.parseDouble(this.value);

				this.pageContext.getOut().append(new DecimalFormat(format).format(d));
			} catch (Exception e) {
				logger.error("输出round标签失败", e);
				this.pageContext.getOut().append(format.replace("#", "0"));
			}
		} catch (IOException e) {
			logger.error("输出round标签失败", e);
		}
		return 6;
	}

	public static void main(String[] args) {
		System.out.println(new DecimalFormat("#.00").format(0.0D).toString());
	}
}