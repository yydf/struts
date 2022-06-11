package cn.coder.struts.jstl.tag;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class ForeachTag extends BodyTagSupport {
	private static final long serialVersionUID = -3853464038808887054L;
	private String var;
	private Iterator<?> iterator;
	private String index;
	private int num = 0;

	public void setVar(String var) {
		this.var = var;
	}

	public void setItems(Object items) {
		this.num = 0;
		this.iterator = null;
		if (items != null) {
			if ((items instanceof Map)) {
				Map<?, ?> map = (Map<?, ?>) items;
				this.iterator = map.entrySet().iterator();
			} else if ((items instanceof Collection)) {
				Collection<?> c = (Collection<?>) items;
				this.iterator = c.iterator();
			} else {
				throw new IllegalStateException("Not supported.");
			}
		}
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public int doStartTag() throws JspException {
		if (process()) {
			return 1;
		}
		return 6;
	}

	private boolean process() {
		if ((null != this.iterator) && (this.iterator.hasNext())) {
			this.pageContext.setAttribute(this.var, this.iterator.next());
			if (this.index != null) {
				this.pageContext.setAttribute(this.index, Integer.valueOf(this.num));
				this.num += 1;
			}
			return true;
		}
		return false;
	}

	public int doAfterBody() throws JspException {
		if (process()) {
			return 2;
		}
		return 6;
	}
}
