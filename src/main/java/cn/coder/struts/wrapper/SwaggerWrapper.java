package cn.coder.struts.wrapper;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import cn.coder.struts.annotation.Param;
import cn.coder.struts.annotation.Request;
import cn.coder.struts.annotation.Request.HttpMethod;
import cn.coder.struts.aop.Aop;
import cn.coder.struts.core.Action;
import cn.coder.struts.core.ActionHandler;
import cn.coder.struts.support.SwaggerSupport;
import cn.coder.struts.view.JSONMap;

public final class SwaggerWrapper {

	private SwaggerSupport swaggerSupport;
	private final JSONMap jsonMap;
	private final String requestUrl;

	public SwaggerWrapper(Class<?> swaggerClazz, String templete, ActionHandler handler) {
		this.swaggerSupport = (SwaggerSupport) Aop.create(swaggerClazz);
		this.requestUrl = this.swaggerSupport.getUrl();
		this.jsonMap = new JSONMap();
		jsonMap.put("swagger", "2.0");
		jsonMap.put("info", new Info("接口文档", "1.0.0"));
		jsonMap.put("host", this.swaggerSupport.getHost());
		jsonMap.put("basePath", "/");
		Class<?>[] controllers = handler.getControllers();
		Set<String> actions = handler.getActions().keySet();
		ArrayList<Tag> tags = new ArrayList<>();
		HashMap<String, HashMap<String, ApiMethod>> map = new HashMap<>();
		Request req;
		for (Class<?> clazz : controllers) {
			req = clazz.getAnnotation(Request.class);
			if (req != null) {
				tags.add(new Tag(req.value(), ""));
				for (String action : actions) {
					if (action.startsWith(req.value() + "/")) {
						map.put(action, test(req.value(), handler.getActions().get(action)));
					}
				}
			}
		}
		jsonMap.put("tags", tags);
		jsonMap.put("schemes", new String[] { "http" });
		jsonMap.put("paths", map);
	}

	private HashMap<String, ApiMethod> test(String tag, Action action) {
		HashMap<String, ApiMethod> map = new HashMap<>();
		HttpMethod[] methods = action.getAllowMethods();
		for (HttpMethod httpMethod : methods) {
			map.put(httpMethod.name().toLowerCase(), new ApiMethod(tag, action));
		}
		return map;
	}

	public String getRequestUrl() {
		return this.swaggerSupport.getUrl();
	}

	public JSONMap jsonResult() {
		return this.jsonMap;
	}

	public boolean isSwaggerPath(String path) {
		return path.equals(this.requestUrl);
	}

	public void clear() {
		this.swaggerSupport = null;
	}

	public final class Info {
		private String version;
		private String title;

		public Info(String title, String version) {
			this.title = title;
			this.version = version;
		}

		public String getVersion() {
			return version;
		}

		public String getTitle() {
			return title;
		}

	}

	public final class Tag {
		private String name;
		private String description;

		public Tag(String name, String desc) {
			this.name = name;
			this.description = desc;
		}

		public String getDescription() {
			return description;
		}

		public String getName() {
			return name;
		}
	}

	public final class ApiMethod {

		private String[] tags;
		private String summary;
		private String[] produces;
		private ArrayList<P> parameters;
		private HashMap<String, R> responses;

		public ApiMethod(String tag, Action action) {
			this.tags = new String[] { tag };
			this.summary = "";
			this.produces = new String[] { "application/json" };
			this.parameters = new ArrayList<>(action.getParameters().length);
			for (Parameter p : action.getParameters()) {
				parameters.add(new P(p.getAnnotation(Param.class)));
			}
			responses = new HashMap<>();
			responses.put("200", new R("Result ok"));
			responses.put("500", new R("Internal server error"));
		}

		public String[] getTags() {
			return this.tags;
		}

		public String getSummary() {
			return summary;
		}

		public String[] getProduces() {
			return produces;
		}
	}

	public final class P {

		private String name;
		private String in;
		private boolean required;

		public P(Param param) {
			if (param == null)
				return;
			this.name = param.value();
			this.in = "query";
			this.required = true;
		}

		public String getName() {
			return name;
		}

		public String getIn() {
			return in;
		}

		public boolean isRequired() {
			return required;
		}
	}

	public final class R {
		private String description;

		public R(String desc) {
			this.description = desc;
		}

		public String getDescription() {
			return description;
		}
	}
}