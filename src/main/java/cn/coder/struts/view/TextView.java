package cn.coder.struts.view;

import cn.coder.struts.support.ServletWebRequest;

public final class TextView extends AbstractView {

	private static final String CONTENT_TYPE_TEXT = "text/plain";

	@Override
	public boolean supports(Object result) {
		return (result instanceof String);
	}

	@Override
	public void render(ServletWebRequest req, Object str) throws Exception {
		req.setContentType(CONTENT_TYPE_TEXT);
		// 输出文本
		renderText(str.toString(), req.supportGzip(), req.getResponse());
	}

}
