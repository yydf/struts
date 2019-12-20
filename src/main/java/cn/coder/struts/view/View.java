package cn.coder.struts.view;

import cn.coder.struts.support.ServletWebRequest;

public interface View {

	boolean supports(Object result);

	void render(ServletWebRequest req, Object result) throws Exception;

}
