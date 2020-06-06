package cn.coder.struts.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class DefaultView extends AbstractView {

	private static final String CONTENT_TYPE_TEXT = "text/plain";

	@Override
	public boolean supports(Object result) {
		return result != null;
	}

	@Override
	public void render(Object result, HttpServletRequest req, HttpServletResponse res) throws Exception {
		res.setContentType(CONTENT_TYPE_TEXT);
		// 输出文本
		renderText(result.toString(), supportGzip(req), res);
	}

}
