package cn.coder.struts.view;

import java.io.PrintWriter;

import cn.coder.struts.support.ServletWebRequest;

public final class TextView implements View {

	private static final String CONTENT_TYPE_TEXT = "text/plain";

	@Override
	public boolean supports(Object result) {
		return (result instanceof String);
	}

	@Override
	public void render(ServletWebRequest req, Object result) throws Exception {
		req.setContentType(CONTENT_TYPE_TEXT);
		PrintWriter pw = req.getWriter();
		pw.write(result.toString());
		pw.close();
	}

}
