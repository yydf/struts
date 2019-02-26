package cn.coder.struts.support;

import javax.servlet.ServletContext;

public interface WebInitializer {
	void onStartup(ServletContext ctx);
}
