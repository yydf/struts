package cn.coder.struts.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface View {

	boolean supports(Object result);

	void render(Object result, HttpServletRequest req, HttpServletResponse res) throws Exception;

}
