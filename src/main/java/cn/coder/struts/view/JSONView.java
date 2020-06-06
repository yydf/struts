package cn.coder.struts.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.mvc.JSONMap;
import cn.coder.struts.util.StringUtils;

public final class JSONView extends AbstractView {
	private static final String CONTENT_TYPE_JSON = "application/json";

	@Override
	public boolean supports(Object result) {
		return (result instanceof JSONMap);
	}

	@Override
	public void render(Object result, HttpServletRequest req, HttpServletResponse res) throws Exception {
		String json = result.toString();
		// 判断jsonp
		String callback = req.getParameter("callback");
		if (!StringUtils.isEmpty(callback))
			json = callback + "(" + json + ")";
		res.setContentType(CONTENT_TYPE_JSON);
		// 输出json
		renderText(json, supportGzip(req), res);
	}
}
