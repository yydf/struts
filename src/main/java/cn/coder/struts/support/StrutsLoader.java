package cn.coder.struts.support;

import javax.servlet.ServletContext;

public abstract class StrutsLoader {

	public abstract void onStartup(ServletContext ctx);

	public abstract void destroy();
}
