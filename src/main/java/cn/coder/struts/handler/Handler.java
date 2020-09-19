package cn.coder.struts.handler;

import javax.servlet.http.HttpServletRequest;

public interface Handler {

	SimpleExecutor getExecutor(HttpServletRequest req);

}
