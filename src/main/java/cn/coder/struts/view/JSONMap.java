package cn.coder.struts.view;

import java.util.HashMap;
import java.util.Map;

import cn.coder.struts.wrapper.JSONWrapper;

public final class JSONMap {

	private final HashMap<String, Object> data = new HashMap<>();
	private final JSONWrapper wrapper = new JSONWrapper();
	
	public static JSONMap success() {
		return new JSONMap().put("success", true).put("errcode", 0).put("errmsg", "ok");
	}
	
	public static JSONMap error(int errcode, String errmsg) {
		return new JSONMap().put("success", false).put("errcode", errcode).put("errmsg", errmsg);
	}

	public JSONMap put(String name, Object value) {
		this.data.put(name, value);
		return this;
	}

	public JSONMap putAll(Map<String, Object> values) {
		this.data.putAll(values);
		return this;
	}

	public void clear() {
		this.data.clear();
	}

	@Override
	public String toString() {
		return wrapper.write(data);
	}
}
