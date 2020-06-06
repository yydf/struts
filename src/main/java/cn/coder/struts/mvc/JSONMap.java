package cn.coder.struts.mvc;

import java.util.HashMap;
import java.util.Map;

import cn.coder.struts.wrapper.JSONWrapper;

public final class JSONMap {
	private final Map<String, Object> data = new HashMap<>();
	private final JSONWrapper wrapper = new JSONWrapper();

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
	public String toString() {
		return wrapper.write(this.data);
	}
}
