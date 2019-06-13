package cn.coder.struts.support;

import javax.servlet.ServletContext;

/**
 * web运行的初始化接口，实现接口的类会在项目启动时调用
 * 
 * @author YYDF
 *
 */
public interface WebInitializer {
	void onStartup(ServletContext ctx);

	void destroy();
}
