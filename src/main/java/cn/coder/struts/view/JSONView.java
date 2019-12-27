package cn.coder.struts.view;

import cn.coder.struts.support.JSONMap;
import cn.coder.struts.support.ServletWebRequest;
import cn.coder.struts.util.StringUtils;

public final class JSONView extends AbstractView {
	private static final String CONTENT_TYPE_JSON = "application/json";

	@Override
	public boolean supports(Object result) {
		return (result instanceof JSONMap);
	}

	@Override
	public void render(ServletWebRequest req, Object result) throws Exception {
		String json = result.toString();
		// 判断jsonp
		String callback = req.getParameter("callback");
		if (!StringUtils.isEmpty(callback))
			json = callback + "(" + json + ")";
		req.setContentType(CONTENT_TYPE_JSON);
		// 输出json
		renderText(json, req.supportGzip(), req.getResponse());
	}
}
