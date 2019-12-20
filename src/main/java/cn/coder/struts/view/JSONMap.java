package cn.coder.struts.view;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import cn.coder.struts.support.ServletWebRequest;
import cn.coder.struts.wrapper.JSONWrapper;

public class JSONMap implements View {
	private final Map<String, Object> data = new HashMap<>();
	private final JSONWrapper wrapper = new JSONWrapper();
	private static final String CONTENT_TYPE_JSON = "application/json";

	public static JSONMap success() {
		return new JSONMap().put("success", true).put("errcode", 0).put("errmsg", "ok");
	}

	public static JSONMap error(int errcode, String errmsg) {
		return new JSONMap().put("success", false).put("errcode", errcode).put("errmsg", errmsg);
	}

	public JSONMap put(String key, Object obj) {
		this.data.put(key, obj);
		return this;
	}

	public JSONMap putAll(Map<String, Object> map) {
		this.data.putAll(map);
		return this;
	}

	public Map<String, Object> getData() {
		return this.data;
	}

	@Override
	public boolean supports(Object result) {
		return (result instanceof JSONMap);
	}

	@Override
	public void render(ServletWebRequest req, Object result) throws Exception {
		String json = wrapper.write(((JSONMap) result).getData());
		req.setContentType(CONTENT_TYPE_JSON);
		PrintWriter pw = req.getWriter();
		pw.write(json);
		pw.close();
	}

}
